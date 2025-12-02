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
import org.digilib.library.models.Reservation;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.ReservationData;
import org.digilib.library.models.dto.user.UserData;
import org.digilib.library.models.dto.user.UserUpdate;
import org.digilib.library.openapi.ErrorResponse;
import org.digilib.library.repositories.ReservationRepository;
import org.digilib.library.services.JwtService;
import org.digilib.library.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operations related to users and their reservations")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final ReservationRepository reservationRepository;

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns the currently authenticated user with roles extracted from the access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserData.class),
                            examples = @ExampleObject(
                                    name = "CurrentUser",
                                    value = """
                                            {
                                              "id": 1,
                                              "email": "user@example.com",
                                              "firstName": "John",
                                              "lastName": "Doe",
                                              "roles": ["USER"],
                                              "enabled": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated or invalid token",
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
                                              "path": "/api/users/me",
                                              "timestamp": "2025-12-02T22:25:00Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(
            @AuthenticationPrincipal User currentUser,
            @Parameter(
                    description = "Bearer access token",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");
        List<String> roles = jwtService.extractRolesFromToken(token);
        return ResponseEntity.ok(UserData.wrapUser(currentUser, roles));
    }

    @Operation(
            summary = "Get current user's reservations",
            description = "Returns a paginated list of reservations for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of reservations",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "CurrentUserReservations",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 10,
                                                  "isbn": "9780132350884",
                                                  "title": "Clean Code",
                                                  "barcode": "LIB-001-00010",
                                                  "reservedAt": "2025-12-02T21:30:00Z",
                                                  "library": {
                                                    "id": 3,
                                                    "name": "Central Library",
                                                    "city": "Vilnius"
                                                  }
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
                    description = "Invalid page or sorts parameters",
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
                                              "path": "/api/users/me/reservations",
                                              "timestamp": "2025-12-02T22:25:01Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
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
                                              "path": "/api/users/me/reservations",
                                              "timestamp": "2025-12-02T22:25:02Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me/reservations")
    public ResponseEntity<Page<ReservationData>> getCurrentUserReservations(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=reservedAt&sorts=isbn
                            """,
                    array = @ArraySchema(schema = @Schema(example = "reservedAt"))
            )
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, Reservation.class);

        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );
        Page<Reservation> reservations = reservationRepository
                .findAllByUserId(currentUser.getId(), pageable);

        return ResponseEntity.ok(reservations.map(ReservationData::of));
    }


    @Operation(
            summary = "Get all users (admin only)",
            description = "Returns a paginated list of all users. The current admin may be excluded at service level.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of users",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "UsersPage",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 2,
                                                  "email": "user@example.com",
                                                  "firstName": "John",
                                                  "lastName": "Doe",
                                                  "roles": ["USER"],
                                                  "enabled": true
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
                    description = "Invalid page or sorts parameters",
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
                                              "path": "/api/users/all",
                                              "timestamp": "2025-12-02T22:25:03Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
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
                                              "path": "/api/users/all",
                                              "timestamp": "2025-12-02T22:25:03Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Current user is not an admin",
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
                                              "path": "/api/users/all",
                                              "timestamp": "2025-12-02T22:25:04Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<UserData>> getAllUsers(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Page number starting from 1", example = "1")
            @RequestParam(name = "page") int pageNumber,
            @Parameter(
                    description = """
                            Sorting fields (property names).
                            Multiple values are passed by repeating the parameter, e.g.:
                            ?page=1&sorts=email,lastName
                            """,
                    array = @ArraySchema(schema = @Schema(example = "email"))
            )
            @RequestParam(name = "sorts") String[] sorts) {

        InvalidRequestParamException.notPositivePage(pageNumber);
        InvalidRequestParamException.notValidSorts(sorts, User.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePrivate())
                .body(userService.findAll(currentUser.getId(), pageable));

    }

    @Operation(
            summary = "Update a user",
            description = "Partially updates a user. Typically, a user can update their own profile; admins may update others (enforced in service layer).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserData.class),
                            examples = @ExampleObject(
                                    name = "UserUpdated",
                                    value = """
                                            {
                                              "id": 1,
                                              "email": "user@example.com",
                                              "firstName": "Johnny",
                                              "lastName": "Doe",
                                              "roles": ["USER"],
                                              "enabled": true
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
                                              "path": "/api/users/1",
                                              "timestamp": "2025-12-02T22:25:05Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
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
                                              "path": "/api/users/1",
                                              "timestamp": "2025-12-02T22:25:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – current user not allowed to update this user",
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
                                              "path": "/api/users/2",
                                              "timestamp": "2025-12-02T22:25:06Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "User with id '99' not found",
                                              "path": "/api/users/99",
                                              "timestamp": "2025-12-02T22:25:07Z"
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
                                    name = "UserValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/users/1",
                                              "timestamp": "2025-12-02T22:25:07Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserData> updateUser(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "ID of the user to update", example = "1")
            @PathVariable long id,
            @RequestBody @Valid UserUpdate userUpdate) {

        return ResponseEntity.ok(userService.updateUser(currentUser, id, userUpdate));
    }

    @Operation(
            summary = "Disable a user (admin only)",
            description = "Disables a user account by ID, preventing future logins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User disabled"),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
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
                                              "path": "/api/users/2/disable",
                                              "timestamp": "2025-12-02T22:25:08Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Current user is not an admin",
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
                                              "path": "/api/users/2/disable",
                                              "timestamp": "2025-12-02T22:25:08Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "User with id '99' not found",
                                              "path": "/api/users/99/disable",
                                              "timestamp": "2025-12-02T22:25:09Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/disable")
    public ResponseEntity<?> disableUserById(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "ID of the user to disable", example = "2")
            @PathVariable long id) {

        userService.disableUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Enable a user (admin only)",
            description = "Enables a previously disabled user account by ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User enabled"),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
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
                                              "path": "/api/users/2/enable",
                                              "timestamp": "2025-12-02T22:25:10Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Current user is not an admin",
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
                                              "path": "/api/users/2/enable",
                                              "timestamp": "2025-12-02T22:25:10Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    value = """
                                            {
                                              "code": 2,
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "User with id '99' not found",
                                              "path": "/api/users/99/enable",
                                              "timestamp": "2025-12-02T22:25:11Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUserById(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "ID of the user to enable", example = "2")
            @PathVariable long id) {

        userService.enableUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
