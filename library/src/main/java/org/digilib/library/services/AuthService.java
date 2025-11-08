package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.auth.AuthData;
import org.digilib.library.models.dto.auth.LoginDto;
import org.digilib.library.models.dto.auth.UserData;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthData authenticateUser(LoginDto login) {
        var authToken = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        var principal = (User) authentication.getPrincipal();

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal.getUsername());

        return new AuthData(access, refresh, UserData.wrapUser(principal));

    }

    public String issueAccessToken(UserDetails user) {
        return jwtService.generateAccessToken(user);
    }
}
