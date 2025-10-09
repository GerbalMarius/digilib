package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.AuthorCreateView;
import org.digilib.library.models.dto.AuthorData;
import org.digilib.library.models.dto.AuthorUpdateView;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.services.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.BACK_URL;
import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;


    @GetMapping("/authors")
    public ResponseEntity<Page<AuthorData>> getAuthors(@RequestParam(name = "page") int pageNumber,
                                                       @RequestParam(name = "sorts") String[] sorts){

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Author.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<AuthorData> authorPage = authorService.findAll(pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorPage);
    }



    @GetMapping("/authors/{id}/books")
    public ResponseEntity<Page<BookData>> getBooksByAuthor(@PathVariable long id,
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Book.class);

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<BookData> bookPage = authorService.findBooksByAuthor(id, pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(bookPage);
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorData> createAuthor(@RequestBody AuthorCreateView authorData){

        return ResponseEntity.created(URI.create(BACK_URL + "/api/authors"))
                .body(authorService.createFrom(authorData));
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorData> getAuthor(@PathVariable long id){
        AuthorData author = authorService.findById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(author);
    }

    @PatchMapping("/authors/{id}")
    public ResponseEntity<AuthorData> updateAuthor(@PathVariable long id, @RequestBody @Valid AuthorUpdateView updateData){

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorService.update(id, updateData));
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable long id){
        authorService.deleteById(id);

        return ResponseEntity.noContent().build();
    }


}
