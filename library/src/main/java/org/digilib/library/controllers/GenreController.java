package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.exceptions.InvalidRequestParamException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Book;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.author.AuthorData;
import org.digilib.library.models.dto.book.BookData;
import org.digilib.library.models.dto.genre.GenreCreateView;
import org.digilib.library.models.dto.genre.GenreData;
import org.digilib.library.models.dto.genre.GenreUpdateView;
import org.digilib.library.services.AuthorService;
import org.digilib.library.services.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.BACK_URL;
import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GenreController {

    private final GenreService genreService;

    private final AuthorService authorService;


    @GetMapping("/genres")
    public ResponseEntity<List<GenreData>> getAllGenres() {


        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findAll());
    }

    @GetMapping("/genres/{id}")
    public ResponseEntity<GenreData> getGenre(@PathVariable long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findGenreDataById(id));
    }

    @GetMapping("/genres/{id}/books")
    public ResponseEntity<Page<BookData>> getBooksByGenreId(@PathVariable long id,
                                                            @RequestParam(name = "page") int pageNumber,
                                                            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Book.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findBooksByGenreId(id, pageable));

    }

    @GetMapping("/genres/{id}/authors")
    public ResponseEntity<Page<AuthorData>> getAuthorsByGenreId(@PathVariable long id,
                                                                @RequestParam(name = "page") int pageNumber,
                                                                @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Author.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findAuthorsByGenre(id, pageable));
    }

    @GetMapping("/genres/{genreId}/authors/{authorId}")
    public ResponseEntity<AuthorData> getGenreAuthor(@PathVariable long genreId,
                                                     @PathVariable long authorId) {

        Genre genre = genreService.findById(genreId);

        AuthorData authorData = authorService.findAuthorByGenre(authorId, genre);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorData);
    }

    @GetMapping("/genres/{genreId}/authors/{authorId}/books")
    public ResponseEntity<Page<BookData>> getGenreAuthorBooks(@PathVariable long genreId,
                                                              @PathVariable long authorId,
                                                              @RequestParam(name = "page") int pageNumber,
                                                              @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, Book.class);

        Genre genre = genreService.findById(genreId);

        AuthorData authorData = authorService.findAuthorByGenre(authorId, genre);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<BookData> bookPage = authorService.findBooksByAuthor(authorData.id(), pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(bookPage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/genres")
    public ResponseEntity<GenreData> createGenre(@RequestBody @Valid GenreCreateView genreCreateData) {
        return ResponseEntity.created(URI.create(BACK_URL + "/api/genres"))
                .body(genreService.createGenre(genreCreateData));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PutMapping("/genres/{id}")
    public ResponseEntity<GenreData> updateGenre(@PathVariable long id,
                                                 @RequestBody @Valid GenreUpdateView genreUpdateData) {
        return ResponseEntity.ok(genreService.updateGenre(id, genreUpdateData));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/genres/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }


}
