package org.digilib.library.services;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.ExpiredRefreshTokenException;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.auth.AuthData;
import org.digilib.library.models.dto.auth.LoginDto;
import org.digilib.library.models.dto.user.UserData;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    public AuthData authenticateUser(LoginDto login) {
        var authToken = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        var principal = (User) authentication.getPrincipal();

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal.getUsername());

        List<String> roles = jwtService.extractRolesFromToken(access);

        return new AuthData(access, refresh, UserData.wrapUser(principal, roles));

    }

    public AuthData refreshToken(String refreshToken) {
        if (!jwtService.isRefreshValid(refreshToken)) {
            throw new ExpiredRefreshTokenException("Expired refresh token",
                    jwtService.extractFromToken(refreshToken, claims -> claims));
        }
        String username = jwtService.extractFromToken(refreshToken, Claims::getSubject);
        User principal = (User) userDetailsService.loadUserByUsername(username);

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal.getUsername());

        List<String> roles = jwtService.extractRolesFromToken(access);

        return new AuthData(access, refresh, UserData.wrapUser(principal, roles));
    }

    public boolean isExpiredToken(String token) {
        return jwtService.isExpired(token);
    }
}
