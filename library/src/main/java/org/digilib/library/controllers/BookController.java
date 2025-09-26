package org.digilib.library.controllers;

import lombok.RequiredArgsConstructor;
import org.digilib.library.models.Book;
import org.digilib.library.repositories.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;

    @GetMapping("/books")
    @ResponseStatus(value = HttpStatus.OK, reason = "returns books successfully")
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "isbn") String sort) {

        Pageable pageable = PageRequest.of(pageNumber - 1, 10, Sort.by(sort));

        Page<Book> books = bookRepository.findAll(pageable);

      return ResponseEntity.ok(books.getContent());
    }

    @PostMapping("/books")
    @ResponseStatus(value = HttpStatus.CREATED, reason = "creates books")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookRepository.save(book));
    }
}
