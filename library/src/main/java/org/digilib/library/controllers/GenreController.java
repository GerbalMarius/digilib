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
import org.digilib.library.openapi.ErrorResponse;
import org.digilib.library.services.AuthorService;
import org.digilib.library.services.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Genres", description = "Operations on genres, their authors and books")
public class GenreController {

    private final GenreService genreService;
    private final AuthorService authorService;

    @Operation(
            summary = "Get all genres",
            description = "Returns a list of all genres."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of genres",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GenreData.class)),
                            examples = @ExampleObject(
                                    name = "GenresList",
                                    value = """
                                            [
                                              { "id": 1, "name": "Science Fiction" },
                                              { "id": 2, "name": "Fantasy" }
                                            ]
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres")
    public ResponseEntity<List<GenreData>> getAllGenres() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findAll());
    }

    @Operation(
            summary = "Get a single genre",
            description = "Retrieves a genre by ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Genre found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenreData.class),
                            examples = @ExampleObject(
                                    name = "GenreFound",
                                    value = """
                                            { "id": 1, "name": "Science Fiction" }
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
                                              "message": "Genre with id '99' not found",
                                              "path": "/api/genres/99",
                                              "timestamp": "2025-12-02T21:55:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres/{id}")
    public ResponseEntity<GenreData> getGenre(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long id) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findGenreDataById(id));
    }

    

    @Operation(
            summary = "Get books by genre",
            description = "Returns a paginated list of books for the given genre."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of books for the genre",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "BooksByGenre",
                                    value = """
                                            {
                                              "content": [
                                                { "isbn": "9780132350884", "title": "Clean Code" }
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
                                              "path": "/api/genres/1/books",
                                              "timestamp": "2025-12-02T21:55:01Z"
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
                                              "message": "Genre with id '99' not found",
                                              "path": "/api/genres/99/books",
                                              "timestamp": "2025-12-02T21:55:01Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres/{id}/books")
    public ResponseEntity<Page<BookData>> getBooksByGenreId(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long id,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts,publicationDate
                            """,
                    array = @ArraySchema(schema = @Schema(example = "title"))
            )
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

    

    @Operation(
            summary = "Get authors by genre",
            description = "Returns a paginated list of authors for the given genre."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of authors for the genre",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "AuthorsByGenre",
                                    value = """
                                            {
                                              "content": [
                                                { "id": 1, "firstName": "George", "lastName": "Orwell" }
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
                                              "path": "/api/genres/1/authors",
                                              "timestamp": "2025-12-02T21:55:02Z"
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
                                              "message": "Genre with id '99' not found",
                                              "path": "/api/genres/99/authors",
                                              "timestamp": "2025-12-02T21:55:02Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres/{id}/authors")
    public ResponseEntity<Page<AuthorData>> getAuthorsByGenreId(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long id,
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

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(genreService.findAuthorsByGenre(id, pageable));
    }

    @Operation(
            summary = "Get an author within a genre",
            description = "Returns an author that belongs to the given genre."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Author in genre found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthorData.class),
                            examples = @ExampleObject(
                                    name = "GenreAuthor",
                                    value = """
                                            {
                                              "id": 1,
                                              "firstName": "George",
                                              "lastName": "Orwell"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre or author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "GenreOrAuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '5' not found in genre '2'",
                                              "path": "/api/genres/2/authors/5",
                                              "timestamp": "2025-12-02T21:55:03Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres/{genreId}/authors/{authorId}")
    public ResponseEntity<AuthorData> getGenreAuthor(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long genreId,
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long authorId) {

        Genre genre = genreService.findById(genreId);
        AuthorData authorData = authorService.findAuthorByGenre(authorId, genre);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(authorData);
    }


    @Operation(
            summary = "Get books for an author within a genre",
            description = "Returns a paginated list of books for a given author and genre."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of books for the author in the genre",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "GenreAuthorBooks",
                                    value = """
                                            {
                                              "content": [
                                                { "isbn": "9780132350884", "title": "Clean Code" }
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
                                              "path": "/api/genres/1/authors/1/books",
                                              "timestamp": "2025-12-02T21:55:04Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre or author not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "GenreOrAuthorNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '2'",
                                              "path": "/api/genres/2/authors/5/books",
                                              "timestamp": "2025-12-02T21:55:04Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/genres/{genreId}/authors/{authorId}/books")
    public ResponseEntity<Page<BookData>> getGenreAuthorBooks(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long genreId,
            @Parameter(description = "ID of the author", example = "1")
            @PathVariable long authorId,
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


    @Operation(
            summary = "Create a new genre",
            description = "Creates a new genre.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Genre created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenreData.class),
                            examples = @ExampleObject(
                                    name = "GenreCreated",
                                    value = """
                                            { "id": 10, "name": "Mystery" }
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
                                              "path": "/api/genres",
                                              "timestamp": "2025-12-02T21:55:05Z"
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
                                              "path": "/api/genres",
                                              "timestamp": "2025-12-02T21:55:05Z"
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
                                              "path": "/api/genres",
                                              "timestamp": "2025-12-02T21:55:05Z"
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
                                    name = "GenreValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/genres",
                                              "timestamp": "2025-12-02T21:55:05Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/genres")
    public ResponseEntity<GenreData> createGenre(
            @RequestBody @Valid GenreCreateView genreCreateData) {

        GenreData saved = genreService.createGenre(genreCreateData);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(saved);
    }

    

    @Operation(
            summary = "Update a genre",
            description = "Replaces a genre's data.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Genre updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GenreData.class),
                            examples = @ExampleObject(
                                    name = "GenreUpdated",
                                    value = """
                                            { "id": 1, "name": "Sci-Fi" }
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
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:06Z"
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
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:06Z"
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
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:06Z"
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
                                              "message": "Genre with id '99' not found",
                                              "path": "/api/genres/99",
                                              "timestamp": "2025-12-02T21:55:06Z"
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
                                    name = "GenreValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:06Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PutMapping("/genres/{id}")
    public ResponseEntity<GenreData> updateGenre(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long id,
            @RequestBody @Valid GenreUpdateView genreUpdateData) {

        return ResponseEntity.ok(genreService.updateGenre(id, genreUpdateData));
    }

    

    @Operation(
            summary = "Delete a genre",
            description = "Deletes a genre by ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Genre deleted"),
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
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:07Z"
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
                                              "path": "/api/genres/1",
                                              "timestamp": "2025-12-02T21:55:07Z"
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
                                              "message": "Genre with id '99' not found",
                                              "path": "/api/genres/99",
                                              "timestamp": "2025-12-02T21:55:07Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/genres/{id}")
    public ResponseEntity<?> deleteGenre(
            @Parameter(description = "ID of the genre", example = "1")
            @PathVariable long id) {

        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}
