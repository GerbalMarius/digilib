package org.digilib.library.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.digilib.library.models.dto.auth.LoginDto;
import org.digilib.library.models.dto.auth.LoginResponse;
import org.digilib.library.models.dto.auth.RegisterDto;
import org.digilib.library.models.dto.auth.UserData;

import org.digilib.library.services.AuthService;
import org.digilib.library.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.digilib.library.LibraryApplication.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserService userService;


    @PostMapping("/sign-up/user")
    public ResponseEntity<UserData> registerUser(@RequestBody @Valid RegisterDto registerData) {
        UserData registeredUser = userService.signupUser(registerData, List.of("USER"));
        return ResponseEntity.created(URI.create(BACK_URL + "/api/auth/sign-up/user"))
                .body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody @Valid LoginDto loginData,
                                                   HttpServletResponse response) {
        var auth = authService.authenticateUser(loginData);

        setRefreshCookie(response, auth.refresh());

        return ResponseEntity.ok()
                .body(LoginResponse.of(auth.access(), authService.isExpiredToken(auth.access()), auth.user()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletResponse response,
                                                      @CookieValue("refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        var auth = authService.refreshToken(refreshToken);

        setRefreshCookie(response, auth.refresh());
        return ResponseEntity.ok(LoginResponse.of(auth.access(), authService.isExpiredToken(auth.access()), auth.user()));
    }

    private void setRefreshCookie(HttpServletResponse res, String value) {
        Cookie c = new Cookie("refresh_token", value);
        c.setHttpOnly(true);
        c.setSecure(true);

        c.setPath("/api/auth");
        c.setMaxAge(7 * 24 * 60 * 60);
        res.addCookie(c);
    }
    private void clearRefreshCookie(HttpServletResponse res) {
        Cookie c = new Cookie("refresh_token", null);
        c.setHttpOnly(true);
        c.setSecure(true);

        c.setPath("/api/auth");
        c.setMaxAge(0);
        res.addCookie(c);
    }
}
