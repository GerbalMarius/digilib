package org.digilib.library.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public final class Cookies {
    public static final String REFRESH_COOKIE = "refresh_token";
    private Cookies(){}

    public static void setRefreshCookie(HttpServletResponse res, String value) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        res.addHeader("Set-Cookie", cookie.toString());
    }
    public static void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("None")
                .build();

        res.addHeader("Set-Cookie", cookie.toString());
    }
}
