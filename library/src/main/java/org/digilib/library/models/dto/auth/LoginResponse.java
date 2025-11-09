package org.digilib.library.models.dto.auth;

import org.digilib.library.models.dto.user.UserData;

public record LoginResponse(
        String accessToken,
        UserData user
) {
    public static LoginResponse of(String accessToken, UserData user) {
        return new LoginResponse(
                accessToken,
                user
        );
    }
}
