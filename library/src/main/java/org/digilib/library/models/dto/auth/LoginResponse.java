package org.digilib.library.models.dto.auth;

public record LoginResponse(
        String accessToken,
        boolean isExpired,
        UserData user
) {
    public static LoginResponse of(String accessToken, boolean isExpired, UserData user) {
        return new LoginResponse(
                accessToken,
                isExpired,
                user
        );
    }
}
