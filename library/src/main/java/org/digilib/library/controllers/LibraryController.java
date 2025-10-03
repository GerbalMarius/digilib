package org.digilib.library.controllers;

import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.InvalidRequestParamException;
import org.digilib.library.models.Library;
import org.digilib.library.models.dto.LibraryData;
import org.digilib.library.services.LibraryService;
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
public class LibraryController {
    private final LibraryService libraryService;


    @GetMapping("/libraries")
    public ResponseEntity<Page<LibraryData>> getAllLibraries(
            @RequestParam(name = "page") int pageNumber,
            @RequestParam(name = "sorts") String[] sorts
    ){
        InvalidRequestParamException.negativePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, Library.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(libraryService.findAll(pageable));
    }
}
