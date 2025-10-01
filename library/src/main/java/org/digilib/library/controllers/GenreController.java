package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Book;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.models.dto.GenreCreateView;
import org.digilib.library.models.dto.GenreData;
import org.digilib.library.services.GenreService;
import org.digilib.library.utils.Params;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/genres")
    public ResponseEntity<List<GenreData>> getAllGenres(@RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.throwIf(sorts, "sorts", strings -> !Params.areValidSorts(strings, Genre.class));


        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findAllViews(Sort.by(sorts)));
    }


    @GetMapping("/genres/{id}")
    public ResponseEntity<GenreData> getGenre(@PathVariable long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findGenreById(id));
    }

    @GetMapping("/genres/{id}/books")
    public ResponseEntity<Page<BookData>> getBooksByGenreId(
            @PathVariable long id,
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.throwIf(pageNumber, "pageNumber", n -> n <= 0);

        InvalidRequestParamException.throwIf(sorts, "sorts", strings -> !Params.areValidSorts(strings, Book.class));

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findBooksByGenreId(id, pageable));

    }

    @PostMapping("/genres")
    public ResponseEntity<GenreData> createGenre(@RequestBody @Valid GenreCreateView  genreCreateData) {
        return ResponseEntity.created(URI.create(BACK_URL + "/api/genres"))
                .body(genreService.createGenre(genreCreateData));
    }


}
