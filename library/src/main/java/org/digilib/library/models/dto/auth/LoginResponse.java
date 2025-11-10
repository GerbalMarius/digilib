package org.digilib.library.models.dto.auth;

public record LoginResponse(
        String accessToken
) {
    public static LoginResponse of(String accessToken) {
        return new LoginResponse(
                accessToken
        );
    }
}
