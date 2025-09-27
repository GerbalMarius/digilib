package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.BookCreateData;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.errors.ResourceNotFoundException;

import org.digilib.library.utils.Params;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookController {

    private static final int PAGE_SIZE = 15;

    private final BookRepository bookRepository;

    @GetMapping("/books")
    public ResponseEntity<Page<Book>> getAllBooks(
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

      return ResponseEntity.status(HttpStatus.OK)
              .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
              .body(bookPage);
    }

    @GetMapping("/books/{isbn}")
    public ResponseEntity<Book> getBook(@PathVariable String isbn) {
        String normalised = isbn.replaceAll("[-\\s]", "");

        Optional<Book> book = bookRepository.findByIsbn(normalised);

        return  book.map(bk ->
                        ResponseEntity.status(HttpStatus.OK)
                                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                                .body(bk))
                    .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));
    }

    @PostMapping("/books")
    public ResponseEntity<String> createBook(@RequestBody @Valid BookCreateData creationData) {

        Book saved = bookRepository.save(Book.createFrom(creationData));

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(saved.getIsbn());
    }
}
