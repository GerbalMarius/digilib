package org.digilib.library.models.dto.auth;

public record AuthData(
        String access,
        String refresh,
        UserData user
) {
}
