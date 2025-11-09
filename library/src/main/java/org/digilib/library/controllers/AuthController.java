package org.digilib.library.controllers;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.AdminCodeMismatchException;
import org.digilib.library.models.dto.auth.LoginDto;
import org.digilib.library.models.dto.auth.LoginResponse;
import org.digilib.library.models.dto.auth.RegisterDto;
import org.digilib.library.models.dto.user.UserData;

import org.digilib.library.services.AuthService;
import org.digilib.library.services.UserService;
import org.digilib.library.utils.Cookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.digilib.library.LibraryApplication.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

    @Value("${security.admin.signup-code}")
    private String adminCode;


    @PostMapping("/signUp/user")
    public ResponseEntity<UserData> registerUser(@RequestBody @Valid RegisterDto registerData) {
        UserData registeredUser = userService.signupUser(registerData, List.of("USER"));
        return ResponseEntity.created(URI.create(BACK_URL + "/api/auth/signUp/user"))
                .body(registeredUser);
    }

    @PostMapping("/signUp/admin")
    public ResponseEntity<UserData> registerAdmin(@RequestBody @Valid RegisterDto registerData) {

        if (!Objects.equals(registerData.adminCode(), adminCode)){
            throw new AdminCodeMismatchException("Invalid adminCode");
        }

        UserData registeredUser = userService.signupUser(registerData, List.of("ADMIN"));
        return ResponseEntity.created(URI.create(BACK_URL + "/api/auth/signUp/admin"))
                .body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginDto loginData,
                                                   HttpServletResponse response) {
        var auth = authService.authenticateUser(loginData);

        Cookies.setRefreshCookie(response, auth.refresh());

        return ResponseEntity.ok()
                .body(LoginResponse.of(auth.access(), auth.user()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletResponse response,
                                                      @CookieValue(Cookies.REFRESH_COOKIE) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        var auth = authService.refreshToken(refreshToken);

        Cookies.setRefreshCookie(response, auth.refresh());
        return ResponseEntity.ok(LoginResponse.of(auth.access(), auth.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookies.clearRefreshCookie(response);

        return ResponseEntity.noContent()
                .build();
    }

}
