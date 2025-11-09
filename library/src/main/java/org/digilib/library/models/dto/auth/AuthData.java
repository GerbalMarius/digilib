package org.digilib.library.models.dto.auth;

import org.digilib.library.models.dto.user.UserData;

public record AuthData(
        String access,
        String refresh,
        UserData user
) {
}
