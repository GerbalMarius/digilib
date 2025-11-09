package org.digilib.library.models.dto.auth;

import org.digilib.library.models.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record UserData(
        long id,
        String email,
        String firstName,
        String lastName,
        List<String> authorities
) {
    public static UserData wrapUser(User user) {
        return new UserData(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}
