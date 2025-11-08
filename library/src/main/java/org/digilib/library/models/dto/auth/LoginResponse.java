package org.digilib.library.models.dto.auth;

public record LoginResponse(
        String accessToken,
        UserData user
) {
    public static LoginResponse of(AuthData authData) {
        return new LoginResponse(
                authData.access(),
                authData.user()
        );
    }
}
