package org.digilib.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.book.LibraryBookData;
import org.digilib.library.openapi.ErrorResponse;
import org.digilib.library.services.BookCopyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Operations for reserving book copies")
public class ReservationController {

    private final BookCopyService bookCopyService;

    @Operation(
            summary = "Reserve a book copy",
            description = """
                    Reserves a book copy for the authenticated user.
                    The copy must be AVAILABLE; otherwise the operation fails.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book copy successfully reserved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LibraryBookData.class),
                            examples = @ExampleObject(
                                    name = "ReservationSuccess",
                                    value = """
                                            {
                                              "copyId": 14,
                                              "isbn": "9780132350884",
                                              "title": "Clean Code",
                                              "language": "EN",
                                              "status": "RESERVED",
                                              "library": {
                                                "id": 3,
                                                "name": "Central Library",
                                                "city": "London"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Copy cannot be reserved due to business rules",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CopyUnavailable",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "Copy is not available for reservation.",
                                              "path": "/api/books/14/reserve",
                                              "timestamp": "2025-12-02T22:20:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
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
                                              "path": "/api/books/14/reserve",
                                              "timestamp": "2025-12-02T22:20:01Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have USER role",
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
                                              "path": "/api/books/14/reserve",
                                              "timestamp": "2025-12-02T22:20:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book copy or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CopyNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "BookCopy with id '99' not found",
                                              "path": "/api/books/99/reserve",
                                              "timestamp": "2025-12-02T22:20:03Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/books/{copyId}/reserve")
    public ResponseEntity<LibraryBookData> reserveBookCopy(
            @Parameter(description = "ID of the book copy to reserve", example = "14")
            @PathVariable long copyId,
            @AuthenticationPrincipal User currentUser) {

        LibraryBookData reserved = bookCopyService.reserveCopy(copyId, currentUser.getEmail());
        return ResponseEntity.ok(reserved);
    }
}
