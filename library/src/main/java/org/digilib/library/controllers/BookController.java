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
import org.digilib.library.errors.exceptions.ResourceNotFoundException;
import org.digilib.library.models.Book;
import org.digilib.library.models.dto.book.BookCreateView;
import org.digilib.library.models.dto.book.BookData;
import org.digilib.library.models.dto.book.BookUpdateView;
import org.digilib.library.models.dto.book.LibraryBookData;
import org.digilib.library.openapi.ErrorResponse;
import org.digilib.library.services.BookCopyService;
import org.digilib.library.services.BookService;
import org.digilib.library.validators.isbn.IsbnValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books", description = "Operations on books and their copies")
public class BookController {

    private final BookService bookService;
    private final BookCopyService bookCopyService;

    

    @Operation(
            summary = "Get all books",
            description = "Returns a paginated list of all books. " +
                    "Sorting is done by one or more entity fields, all in ascending order."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of books",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page or sorts",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidPageOrSorts",
                                    summary = "Page number must be positive",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "page must be positive",
                                              "path": "/api/books",
                                              "details": {
                                                "page": "must be greater than 0"
                                              },
                                              "timestamp": "2025-12-02T21:44:08.430Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InternalError",
                                    value = """
                                            {
                                              "code": 3,
                                              "status": 500,
                                              "error": "INTERNAL_SERVER_ERROR",
                                              "message": "Unexpected error occurred",
                                              "path": "/api/books",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.431Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/books")
    public ResponseEntity<Page<BookData>> getAllBooks(
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,

            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=title,publicationDate
                            """,
                    array = @ArraySchema(
                            schema = @Schema(example = "title")
                    )
            )
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, Book.class);

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        Page<Book> bookPage = bookService.findAll(pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(bookPage.map(BookData::wrapBook));
    }

    

    @Operation(
            summary = "Create a new book",
            description = "Creates a new book with authors and genre.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Book created",
                    content = @Content(schema = @Schema(implementation = BookData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request body",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MalformedBody",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "JSON parse error: Unexpected character",
                                              "path": "/api/books",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.432Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required to access this resource",
                                              "path": "/api/books",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.433Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN or LIBRARIAN role",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/books",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.434Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre or authors not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "GenreOrAuthorsNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Genre with id '5' not found",
                                              "path": "/api/books",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.435Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/books",
                                              "details": {
                                                "title": "must not be blank",
                                                "isbn": "must be a valid ISBN-13"
                                              },
                                              "timestamp": "2025-12-02T21:44:08.436Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/books")
    public ResponseEntity<BookData> createBook(
            @RequestBody @Valid BookCreateView creationData) {

        Book saved = bookService.createBookFrom(creationData);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{isbn}")
                .buildAndExpand(saved.getIsbn())
                .toUri();

        return ResponseEntity.created(location)
                .body(BookData.wrapBook(saved));
    }

    

    @Operation(
            summary = "Get all copies of a book in libraries",
            description = "Returns copies of the book identified by ISBN together with library info."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of copies",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = LibraryBookData.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ISBN",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidIsbn",
                                    value = """
                                            {
                                              "code": 3,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "isbn must be a valid ISBN-13",
                                              "path": "/api/books/invalid/copies",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.437Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Book with isbn '9780132350884' not found",
                                              "path": "/api/books/9780132350884/copies",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.438Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/books/{isbn}/copies")
    public ResponseEntity<List<LibraryBookData>> getBookCopies(
            @Parameter(description = "ISBN-13 of the book", example = "9780132350884")
            @PathVariable String isbn) {

        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        List<LibraryBookData> copies = bookCopyService.findCopiesForBook(isbn);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(copies);
    }

    

    @Operation(
            summary = "Get a single book by ISBN",
            description = "Retrieves book details by its ISBN-13."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ISBN",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidIsbn",
                                    value = """
                                            {
                                              "code": 3,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "isbn must be a valid ISBN-13",
                                              "path": "/api/books/invalid",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.439Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Book with isbn '9780132350884' not found",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.440Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/books/{isbn}")
    public ResponseEntity<BookData> getBook(
            @Parameter(description = "ISBN-13 of the book", example = "9780132350884")
            @PathVariable String isbn) {

        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");

        Optional<Book> book = bookService.findByIsbn(normalised);

        return book.map(bk -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                        .body(BookData.wrapBook(bk)))
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));
    }

    

    @Operation(
            summary = "Update book fields",
            description = "Partially updates a book by ISBN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book updated",
                    content = @Content(schema = @Schema(implementation = BookData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ISBN or malformed body",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidIsbnOrBody",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "isbn must be a valid ISBN-13",
                                              "path": "/api/books/invalid",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.441Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required to access this resource",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.442Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN or LIBRARIAN role",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.443Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or authors not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookOrAuthorsNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Author with id '7' not found",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.444Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation errors",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/books/9780132350884",
                                              "details": {
                                                "title": "must not be blank"
                                              },
                                              "timestamp": "2025-12-02T21:44:08.445Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PatchMapping("/books/{isbn}")
    public ResponseEntity<BookData> updateBook(
            @Parameter(description = "ISBN-13 of the book")
            @PathVariable String isbn,
            @RequestBody @Valid BookUpdateView updateData) {

        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");

        Book existing = bookService.findByIsbn(normalised)
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));

        Book updated = bookService.updateBookFrom(existing, updateData);

        return ResponseEntity.ok(BookData.wrapBook(updated));
    }

    

    @Operation(
            summary = "Delete a book",
            description = "Deletes a book by ISBN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ISBN",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidIsbn",
                                    value = """
                                            {
                                              "code": 3,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "isbn must be a valid ISBN-13",
                                              "path": "/api/books/invalid",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.446Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Full authentication is required to access this resource",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.447Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – missing ADMIN or LIBRARIAN role",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Access is denied",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.448Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BookNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Book with isbn '9780132350884' not found",
                                              "path": "/api/books/9780132350884",
                                              "details": {},
                                              "timestamp": "2025-12-02T21:44:08.449Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/books/{isbn}")
    public ResponseEntity<?> deleteBook(
            @Parameter(description = "ISBN-13 of the book")
            @PathVariable String isbn) {

        InvalidRequestParamException.throwIf(isbn, "isbn", s -> !IsbnValidator.isValidIsbn13(s));

        String normalised = isbn.replaceAll("[-\\s]", "");
        bookService.deleteByIsbn(normalised);

        return ResponseEntity.noContent().build();
    }
}
