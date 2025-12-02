package org.digilib.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.AdminCodeMismatchException;
import org.digilib.library.models.dto.auth.LoginDto;
import org.digilib.library.models.dto.auth.LoginResponse;
import org.digilib.library.models.dto.auth.RegisterDto;
import org.digilib.library.models.dto.user.UserData;
import org.digilib.library.openapi.ErrorResponse;
import org.digilib.library.services.AuthService;
import org.digilib.library.services.UserService;
import org.digilib.library.utils.Cookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, refresh and logout")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${security.admin.signup-code}")
    private String adminCode;

    

    @Operation(
            summary = "Register a regular user",
            description = "Registers a new user with USER role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserData.class),
                            examples = @ExampleObject(
                                    name = "UserRegistered",
                                    value = """
                                            {
                                              "id": 1,
                                              "email": "user@example.com",
                                              "firstName": "John",
                                              "lastName": "Doe",
                                              "roles": ["USER"]
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
                                              "path": "/api/auth/signUp/user",
                                              "timestamp": "2025-12-02T22:15:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "DuplicateEmail",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 409,
                                              "error": "CONFLICT",
                                              "message": "Email 'user@example.com' already in use",
                                              "path": "/api/auth/signUp/user",
                                              "timestamp": "2025-12-02T22:15:01Z"
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
                                    name = "RegisterValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/auth/signUp/user",
                                              "timestamp": "2025-12-02T22:15:01Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signUp/user")
    public ResponseEntity<UserData> registerUser(
            @RequestBody(description = "User registration payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterDto.class),
                            examples = @ExampleObject(
                                    name = "UserRegisterRequest",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "Secret123!",
                                              "firstName": "John",
                                              "lastName": "Doe"
                                            }
                                            """
                            )
                    ))
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterDto registerData) {

        UserData registeredUser = userService.signupUser(registerData, List.of("USER"));

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredUser.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(registeredUser);
    }

    

    @Operation(
            summary = "Register an admin",
            description = "Registers a new user with ADMIN role. Requires a valid adminCode in the payload."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserData.class),
                            examples = @ExampleObject(
                                    name = "AdminRegistered",
                                    value = """
                                            {
                                              "id": 2,
                                              "email": "admin@example.com",
                                              "firstName": "Alice",
                                              "lastName": "Smith",
                                              "roles": ["ADMIN"]
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
                                              "path": "/api/auth/signUp/admin",
                                              "timestamp": "2025-12-02T22:15:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Invalid admin code",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidAdminCode",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 403,
                                              "error": "FORBIDDEN",
                                              "message": "Invalid adminCode",
                                              "path": "/api/auth/signUp/admin",
                                              "timestamp": "2025-12-02T22:15:02Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "DuplicateEmail",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 409,
                                              "error": "CONFLICT",
                                              "message": "Email 'admin@example.com' already in use",
                                              "path": "/api/auth/signUp/admin",
                                              "timestamp": "2025-12-02T22:15:03Z"
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
                                    name = "RegisterValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/auth/signUp/admin",
                                              "timestamp": "2025-12-02T22:15:03Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signUp/admin")
    public ResponseEntity<UserData> registerAdmin(
            @RequestBody(description = "Admin registration payload (includes adminCode)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterDto.class),
                            examples = @ExampleObject(
                                    name = "AdminRegisterRequest",
                                    value = """
                                            {
                                              "email": "admin@example.com",
                                              "password": "Secret123!",
                                              "firstName": "Alice",
                                              "lastName": "Smith",
                                              "adminCode": "MY-ADMIN-CODE"
                                            }
                                            """
                            )
                    ))
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterDto registerData) {

        if (!Objects.equals(registerData.adminCode(), adminCode)) {
            throw new AdminCodeMismatchException("Invalid adminCode");
        }

        UserData registeredUser = userService.signupUser(registerData, List.of("ADMIN"));

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredUser.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(registeredUser);
    }


    @Operation(
            summary = "Login with email and password",
            description = "Authenticates the user and returns an access token. Also sets a refresh token cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginSuccess",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "tokenType": "Bearer",
                                              "user": {
                                                "id": 1,
                                                "email": "user@example.com",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "roles": ["USER"]
                                              }
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
                                              "path": "/api/auth/login",
                                              "timestamp": "2025-12-02T22:15:04Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "BadCredentials",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Bad credentials",
                                              "path": "/api/auth/login",
                                              "timestamp": "2025-12-02T22:15:04Z"
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
                                    name = "LoginValidationError",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 422,
                                              "error": "UNPROCESSABLE_ENTITY",
                                              "message": "Validation failed",
                                              "path": "/api/auth/login",
                                              "timestamp": "2025-12-02T22:15:05Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody(description = "Login payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDto.class),
                            examples = @ExampleObject(
                                    name = "LoginRequest",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "Secret123!"
                                            }
                                            """
                            )
                    ))
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginDto loginData,
            HttpServletResponse response) {

        var auth = authService.authenticateUser(loginData);

        Cookies.setRefreshCookie(response, auth.refresh());

        return ResponseEntity.ok()
                .body(LoginResponse.of(auth.access()));
    }


    @Operation(
            summary = "Refresh access token",
            description = "Uses the refresh token cookie to issue a new access token and refresh cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "RefreshSuccess",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "tokenType": "Bearer",
                                              "user": {
                                                "id": 1,
                                                "email": "user@example.com",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "roles": ["USER"]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingOrExpiredRefresh",
                                    value = """
                                            {
                                              "code": 1,
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "The access token has expired",
                                              "path": "/api/auth/refresh",
                                              "timestamp": "2025-12-02T22:15:06Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            HttpServletResponse response,
            @CookieValue(value = Cookies.REFRESH_COOKIE, required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var auth = authService.refreshToken(refreshToken);

        Cookies.setRefreshCookie(response, auth.refresh());
        return ResponseEntity.ok(LoginResponse.of(auth.access()));
    }


    @Operation(
            summary = "Logout",
            description = "Clears the refresh token cookie. The access token simply expires on its own."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful (cookie cleared)"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookies.clearRefreshCookie(response);

        return ResponseEntity.noContent()
                .build();
    }

}
