package org.digilib.library.models.dto.auth;

import org.digilib.library.models.User;

public record UserData(
        long id,
        String email,
        String firstName,
        String lastName
) {
    public static UserData wrapUser(User user) {
        return new UserData(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
