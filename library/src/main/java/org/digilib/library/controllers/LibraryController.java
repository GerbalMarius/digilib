package org.digilib.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.digilib.library.openapi.ErrorResponse;
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
@Tag(name = "Libraries", description = "Operations on libraries and their book copies")
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(
            summary = "Get all libraries",
            description = "Returns a paginated list of libraries."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of libraries",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "LibrariesPage",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "name": "Central Library",
                                                  "city": "Vilnius"
                                                }
                                              ],
                                              "pageNumber": 0,
                                              "pageSize": 20,
                                              "totalElements": 1,
                                              "totalPages": 1,
                                              "last": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page or sorts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidPageOrSorts",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "page must be positive",
                                              "path": "/api/libraries",
                                              "timestamp": "2025-12-02T22:05:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/libraries")
    public ResponseEntity<Page<LibraryData>> getAllLibraries(
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=city,name
                            """,
                    array = @ArraySchema(schema = @Schema(example = "name"))
            )
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

    @Operation(
            summary = "Get a library by ID",
            description = "Retrieves a single library by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Library found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LibraryData.class),
                            examples = @ExampleObject(
                                    name = "LibraryFound",
                                    value = """
                                            {
                                              "id": 1,
                                              "name": "Central Library",
                                              "city": "Vilnius"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Library with id '99' not found",
                                              "path": "/api/libraries/99",
                                              "timestamp": "2025-12-02T22:05:01Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/libraries/{id}")
    public ResponseEntity<LibraryData> getLibraryById(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long id) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(LibraryData.wrapLibrary(libraryService.findById(id)));
    }

    @Operation(
            summary = "Create a new library",
            description = "Creates a new library.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Library created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LibraryData.class),
                            examples = @ExampleObject(
                                    name = "LibraryCreated",
                                    value = """
                                            {
                                              "id": 10,
                                              "name": "North Branch",
                                              "city": "Kaunas"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MalformedBody",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "JSON parse error",
                                              "path": "/api/libraries",
                                              "timestamp": "2025-12-02T22:05:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required",
                                              "path": "/api/libraries",
                                              "timestamp": "2025-12-02T22:05:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/libraries",
                                              "timestamp": "2025-12-02T22:05:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/libraries",
                                              "timestamp": "2025-12-02T22:05:03Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/libraries")
    public ResponseEntity<LibraryData> createNewLibrary(
            @RequestBody @Valid LibraryCreateView newLibrary) {

        LibraryData saved = libraryService.createNewLibrary(newLibrary);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    @Operation(
            summary = "Delete a library",
            description = "Deletes a library by ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Library deleted"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required",
                                              "path": "/api/libraries/1",
                                              "timestamp": "2025-12-02T22:05:03Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/libraries/1",
                                              "timestamp": "2025-12-02T22:05:03Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Library with id '99' not found",
                                              "path": "/api/libraries/99",
                                              "timestamp": "2025-12-02T22:05:04Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/libraries/{id}")
    public ResponseEntity<?> deleteLibraryById(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long id) {

        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get book copies in a library",
            description = "Returns a paginated list of book copies for a given library."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of book copies in the library",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "LibraryBooks",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 100,
                                                  "isbn": "9780132350884",
                                                  "status": "AVAILABLE",
                                                  "libraryId": 1
                                                }
                                              ],
                                              "pageNumber": 0,
                                              "pageSize": 20,
                                              "totalElements": 1,
                                              "totalPages": 1,
                                              "last": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page or sorts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidPageOrSorts",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "page must be positive",
                                              "path": "/api/libraries/1/books",
                                              "timestamp": "2025-12-02T22:05:05Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Library with id '99' not found",
                                              "path": "/api/libraries/99/books",
                                              "timestamp": "2025-12-02T22:05:05Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/libraries/{id}/books")
    public ResponseEntity<Page<BookCopyData>> getLibraryBooksById(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long id,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=isbn,status
                            """,
                    array = @ArraySchema(schema = @Schema(example = "isbn"))
            )
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

    @Operation(
            summary = "Add a new book copy to a library",
            description = "Creates a new book copy in the given library.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Book copy created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookCopyData.class),
                            examples = @ExampleObject(
                                    name = "BookCopyCreated",
                                    value = """
                                            {
                                              "id": 100,
                                              "isbn": "9780132350884",
                                              "status": "AVAILABLE",
                                              "libraryId": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MalformedBody",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "JSON parse error",
                                              "path": "/api/libraries/1/books",
                                              "timestamp": "2025-12-02T22:05:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required",
                                              "path": "/api/libraries/1/books",
                                              "timestamp": "2025-12-02T22:05:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN or LIBRARIAN role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/libraries/1/books",
                                              "timestamp": "2025-12-02T22:05:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library or book not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryOrBookNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Library with id '99' not found",
                                              "path": "/api/libraries/99/books",
                                              "timestamp": "2025-12-02T22:05:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookCopyValidationError",
                                    value = """
                                        {
                                          "code": 1,
                                          "status": 422,
                                          "error": "UNPROCESSABLE_ENTITY",
                                          "message": "Validation failed",
                                          "path": "/api/libraries/1/books",
                                          "timestamp": "2025-12-02T22:05:06Z"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/libraries/{id}/books")
    public ResponseEntity<BookCopyData> addLibraryBook(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long id,
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

    @Operation(
            summary = "Update a book copy in a library",
            description = "Partially updates a book copy in the given library.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book copy updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookCopyData.class),
                            examples = @ExampleObject(
                                    name = "BookCopyUpdated",
                                    value = """
                                            {
                                              "id": 100,
                                              "isbn": "9780132350884",
                                              "status": "RESERVED",
                                              "libraryId": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MalformedBody",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "JSON parse error",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:07Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:07Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – user not allowed to update this copy",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:07Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library or book copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryOrCopyNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Book copy with id '100' not found in library '1'",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:07Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookCopyValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:07Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    @PatchMapping("/libraries/{libraryId}/books/{bookId}")
    public ResponseEntity<BookCopyData> updateLibraryBook(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long libraryId,
            @Parameter(description = "ID of the book copy", example = "100")
            @PathVariable long bookId,
            @RequestBody @Valid BookCopyUpdateView updateData) {

        Library library = libraryService.findById(libraryId);

        return ResponseEntity.ok(libraryService.updateBookCopy(library, bookId, updateData));
    }

    @Operation(
            summary = "Delete a book copy from a library",
            description = "Deletes a book copy from the given library.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book copy deleted"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:08Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN or LIBRARIAN role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:08Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Library or book copy not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "LibraryOrCopyNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Book copy with id '100' not found in library '1'",
                                              "path": "/api/libraries/1/books/100",
                                              "timestamp": "2025-12-02T22:05:08Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/libraries/{libraryId}/books/{bookId}")
    public ResponseEntity<?> deleteLibraryBook(
            @Parameter(description = "ID of the library", example = "1")
            @PathVariable long libraryId,
            @Parameter(description = "ID of the book copy", example = "100")
            @PathVariable long bookId) {

        Library library = libraryService.findById(libraryId);

        libraryService.deleteBookCopy(library, bookId);

        return ResponseEntity.noContent().build();
    }
}
