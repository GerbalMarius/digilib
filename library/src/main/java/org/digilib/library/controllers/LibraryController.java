package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.InvalidRequestParamException;
import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Library;
import org.digilib.library.models.dto.book.BookCopyCreateView;
import org.digilib.library.models.dto.book.BookCopyData;
import org.digilib.library.models.dto.book.BookCopyUpdateView;
import org.digilib.library.models.dto.library.LibraryCreateView;
import org.digilib.library.models.dto.library.LibraryData;
import org.digilib.library.services.LibraryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class LibraryController {
    private final LibraryService libraryService;


    @GetMapping("/libraries")
    public ResponseEntity<Page<LibraryData>> getAllLibraries(@RequestParam(name = "page") int pageNumber,
                                                             @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
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

    @GetMapping("/libraries/{id}")
    public ResponseEntity<LibraryData> getLibraryById(@PathVariable long id){
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(LibraryData.wrapLibrary(libraryService.findById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/libraries")
    public ResponseEntity<LibraryData> createNewLibrary(@RequestBody @Valid LibraryCreateView newLibrary){

        LibraryData saved = libraryService.createNewLibrary(newLibrary);

         var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/libraries/{id}")
    public ResponseEntity<?> deleteLibraryById(@PathVariable long id){
        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/libraries/{id}/books")
    public ResponseEntity<Page<BookCopyData>> getLibraryBooksById(@PathVariable long id,
                                                                  @RequestParam(name = "page") int pageNumber,
                                                                  @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, BookCopy.class);

        Library library = libraryService.findById(id);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(libraryService.findCopiesByLibrary(library, pageable));

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/libraries/{id}/books")
    public ResponseEntity<BookCopyData> addLibraryBook(@PathVariable long id,
                                                       @RequestBody @Valid BookCopyCreateView newBookCopy) {
        Library library = libraryService.findById(id);


        BookCopyData saved = libraryService.addBookCopyTo(library, newBookCopy);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PatchMapping("/libraries/{libraryId}/books/{bookId}")
    public ResponseEntity<BookCopyData> updateLibraryBook(@PathVariable long libraryId,
                                                          @PathVariable long bookId,
                                                          @RequestBody @Valid BookCopyUpdateView updateData) {

        Library library = libraryService.findById(libraryId);

        return ResponseEntity.ok(libraryService.updateBookCopy(library, bookId, updateData));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/libraries/{libraryId}/books/{bookId}")
    public ResponseEntity<?> deleteLibraryBook(@PathVariable long libraryId,
                                               @PathVariable long bookId) {

        Library library = libraryService.findById(libraryId);

        libraryService.deleteBookCopy(library, bookId);

        return ResponseEntity.noContent().build();
    }
}
