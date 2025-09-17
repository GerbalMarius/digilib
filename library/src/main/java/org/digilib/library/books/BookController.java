package org.digilib.library.books;

import lombok.RequiredArgsConstructor;
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
    @ResponseStatus(value = HttpStatus.OK, reason = "returns books succesfully")
    public ResponseEntity<List<Book>> getAllBooks() {
      return   ResponseEntity.ok()
                .body(bookRepository.findAll());
    }

    @PostMapping("/books")
    @ResponseStatus(value = HttpStatus.CREATED, reason = "creates books")
    public ResponseEntity<Book> createBook(@RequestBody(required = true) Book book) {

    }
}
