package org.digilib.library.controllers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.BookCreateView;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.models.dto.BookUpdateView;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.errors.ResourceNotFoundException;

import org.digilib.library.utils.Params;
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

    private static final int PAGE_SIZE = 15;

    private final BookRepository bookRepository;

    @GetMapping("/books")
    public ResponseEntity<Page<BookData>> getAllBooks(
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.throwIf(pageNumber, "page", num -> num <= 0);

        InvalidRequestParamException.throwIf(sorts, "sorts", strings -> !Params.areValidSorts(strings, Book.class));

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<Book> bookPage = bookRepository.findAll(pageable);

      return ResponseEntity.ok()
              .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
              .body(bookPage.map(BookData::wrapBook));
    }

    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody @Valid BookCreateView creationData) {

        Book saved = bookRepository.save(Book.createFrom(creationData));

        return ResponseEntity.created(URI.create(BACK_URL + "/api/books"))
                .body(saved);
    }

    @GetMapping("/books/{isbn}")
    public ResponseEntity<Book> getBook(@PathVariable String isbn) {
        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");

        Optional<Book> book = bookRepository.findByIsbn(normalised);

        return  book.map(bk -> ResponseEntity.ok()
                                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                                .body(bk))
                    .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));
    }

    @PatchMapping("/books/{isbn}")
    public ResponseEntity<Book> updateBook(@PathVariable String isbn, @RequestBody @Valid BookUpdateView updateData) {

        Book existing = bookRepository.findByIsbn(isbn)
                                      .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));

        Book updated = existing.updateFrom(updateData);

        bookRepository.save(updated);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/books/{isbn}")
    @Transactional
    public ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));
        String normalised = isbn.replaceAll("[-\\s]", "");

        Book found = bookRepository.findByIsbn(normalised)
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));

        bookRepository.delete(found);


        return ResponseEntity.noContent().build();
        }
    }
