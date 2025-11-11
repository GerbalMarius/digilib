package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.InvalidRequestParamException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.author.AuthorCreateView;
import org.digilib.library.models.dto.author.AuthorData;
import org.digilib.library.models.dto.author.AuthorUpdateView;
import org.digilib.library.models.dto.book.BookData;
import org.digilib.library.services.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.concurrent.TimeUnit;

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

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/authors")
    public ResponseEntity<AuthorData> createAuthor(@RequestBody AuthorCreateView authorData){

        AuthorData saved = authorService.createFrom(authorData);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorData> getAuthor(@PathVariable long id){
        AuthorData author = authorService.findById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(author);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PatchMapping("/authors/{id}")
    public ResponseEntity<AuthorData> updateAuthor(@PathVariable long id, @RequestBody @Valid AuthorUpdateView updateData){

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorService.update(id, updateData));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/authors/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable long id){
        authorService.deleteById(id);

        return ResponseEntity.noContent().build();
    }


}
