package org.digilib.library.models.dto.user;

import org.digilib.library.models.Role;
import org.digilib.library.models.User;

import java.util.List;

public record UserData(
        long id,
        String email,
        String firstName,
        String lastName,

        List<String> roles,

        boolean isDisabled
) {
    public static UserData wrapUser(User user, List<String> roles) {
        return new UserData(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles.isEmpty() ? user.getRoles().stream().map(Role::getName).toList() : roles,
                user.isDisabled()
        );
    }
}
