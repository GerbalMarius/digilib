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
import org.digilib.library.models.Author;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.author.AuthorCreateView;
import org.digilib.library.models.dto.author.AuthorData;
import org.digilib.library.models.dto.author.AuthorUpdateView;
import org.digilib.library.models.dto.book.BookData;
import org.digilib.library.openapi.ErrorResponse;
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
@Tag(name = "Authors", description = "Operations on authors and their books")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(
            summary = "Get all authors",
            description = "Returns a paginated list of authors. " +
                    "Sorting is done by one or more entity fields, all in ascending order."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of authors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "AuthorsPage",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "firstName": "George",
                                                  "lastName": "Orwell",
                                                  "birthDate": "1903-06-25",
                                                  "deathDate": "1950-01-21",
                                                  "genreId": 3
                                                }
                                              ],
                                              "pageNumber": 0,
                                              "pageSize": 20,
                                              "totalElements": 1,
                                              "totalPages": 1
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
                                              "path": "/api/authors",
                                              "timestamp": "2025-12-02T21:50:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/authors")
    public ResponseEntity<Page<AuthorData>> getAuthors(
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=lastName,firstName
                            """,
                    array = @ArraySchema(schema = @Schema(example = "lastName"))
            )
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, Author.class);

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<AuthorData> authorPage = authorService.findAll(pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorPage);
    }


    @Operation(
            summary = "Get books by author",
            description = "Returns a paginated list of books written by the given author."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of books for the author",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BookData.class)),
                            examples = @ExampleObject(
                                    name = "BooksByAuthor",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "isbn": "9780132350884",
                                                  "title": "Clean Code",
                                                  "language": "EN"
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
                                              "path": "/api/authors/1/books",
                                              "timestamp": "2025-12-02T21:50:01Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '123' not found",
                                              "path": "/api/authors/123/books",
                                              "timestamp": "2025-12-02T21:50:02Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/authors/{id}/books")
    public ResponseEntity<Page<BookData>> getBooksByAuthor(
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long id,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=title,publicationDate
                            """,
                    array = @ArraySchema(schema = @Schema(example = "title"))
            )
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

    
    @Operation(
            summary = "Create a new author",
            description = "Creates a new author.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Author created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthorData.class),
                            examples = @ExampleObject(
                                    name = "AuthorCreated",
                                    value = """
                                            {
                                              "id": 10,
                                              "firstName": "Agatha",
                                              "lastName": "Christie",
                                              "birthDate": "1890-09-15",
                                              "genreId": 4
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
                                              "path": "/api/authors",
                                              "timestamp": "2025-12-02T21:50:03Z"
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
                                              "path": "/api/authors",
                                              "timestamp": "2025-12-02T21:50:03Z"
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
                                              "path": "/api/authors",
                                              "timestamp": "2025-12-02T21:50:03Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "GenreNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Genre with id '5' not found",
                                              "path": "/api/authors",
                                              "timestamp": "2025-12-02T21:50:04Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/authors")
    public ResponseEntity<AuthorData> createAuthor(
            @RequestBody AuthorCreateView authorData) {

        AuthorData saved = authorService.createFrom(authorData);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    
    @Operation(
            summary = "Get a single author",
            description = "Retrieves an author by ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Author found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthorData.class),
                            examples = @ExampleObject(
                                    name = "AuthorFound",
                                    value = """
                                            {
                                              "id": 1,
                                              "firstName": "George",
                                              "lastName": "Orwell",
                                              "birthDate": "1903-06-25",
                                              "genreId": 3
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '123' not found",
                                              "path": "/api/authors/123",
                                              "timestamp": "2025-12-02T21:50:05Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorData> getAuthor(
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long id) {

        AuthorData author = authorService.findById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(author);
    }

    
    @Operation(
            summary = "Update an author",
            description = "Partially updates an author.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Author updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthorData.class),
                            examples = @ExampleObject(
                                    name = "AuthorUpdated",
                                    value = """
                                            {
                                              "id": 1,
                                              "firstName": "George",
                                              "lastName": "Orwell",
                                              "birthDate": "1903-06-25",
                                              "deathDate": "1950-01-21",
                                              "genreId": 3
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
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:06Z"
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
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:06Z"
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
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '123' not found",
                                              "path": "/api/authors/123",
                                              "timestamp": "2025-12-02T21:50:07Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors (e.g. deathDate before birthDate)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:07Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PatchMapping("/authors/{id}")
    public ResponseEntity<AuthorData> updateAuthor(
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long id,
            @RequestBody @Valid AuthorUpdateView updateData) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorService.update(id, updateData));
    }

    
    @Operation(
            summary = "Delete an author",
            description = "Deletes an author by ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Author deleted"
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
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:08Z"
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
                                              "path": "/api/authors/1",
                                              "timestamp": "2025-12-02T21:50:08Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '123' not found",
                                              "path": "/api/authors/123",
                                              "timestamp": "2025-12-02T21:50:08Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/authors/{id}")
    public ResponseEntity<?> deleteAuthor(
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long id) {

        authorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
