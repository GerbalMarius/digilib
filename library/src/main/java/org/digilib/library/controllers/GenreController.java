package org.digilib.library.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Book;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.GenreData;
import org.digilib.library.services.GenreService;
import org.digilib.library.utils.Params;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GenreController {

    private final GenreService genreService;


    @GetMapping("/genres")
    public ResponseEntity<Page<GenreData>> getAllGenres(
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.throwIf(pageNumber, "page", num -> num <= 0);

        InvalidRequestParamException.throwIf(sorts, "sorts", strings -> !Params.areValidSorts(strings, Genre.class));
        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<Genre> genrePage = genreService.findAll(pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
                .body(genrePage.map(GenreData::wrapGenre));
    }
}
