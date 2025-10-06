package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.BookCreateView;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.models.dto.BookUpdateView;
import org.digilib.library.errors.ResourceNotFoundException;

import org.digilib.library.services.BookService;
import org.digilib.library.validators.IsbnValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<Page<BookData>> getAllBooks(
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Book.class);

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<Book> bookPage = bookService.findAll(pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(bookPage.map(BookData::wrapBook));
    }

    @PostMapping("/books")
    public ResponseEntity<BookData> createBook(@RequestBody @Valid BookCreateView creationData) {

        Book saved = bookService.createBookFrom(creationData);

        return ResponseEntity.created(URI.create(BACK_URL + "/api/books"))
                .body(BookData.wrapBook(saved));
    }

    @GetMapping("/books/{isbn}")
    public ResponseEntity<BookData> getBook(@PathVariable String isbn) {
        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");

        Optional<Book> book = bookService.findByIsbn(normalised);

        return book.map(bk -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                        .body(BookData.wrapBook(bk)))
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));
    }

    @PatchMapping("/books/{isbn}")
    public ResponseEntity<BookData> updateBook(@PathVariable String isbn, @RequestBody @Valid BookUpdateView updateData) {

        Book existing = bookService.findByIsbn(isbn)
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));

        Book updated = bookService.updateBookFrom(existing, updateData);


        return ResponseEntity.ok(BookData.wrapBook(updated));
    }

    @DeleteMapping("/books/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");

        bookService.deleteByIsbn(normalised);


        return ResponseEntity.noContent().build();
    }
}
