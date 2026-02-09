package com.danil.appliances.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@UtilityClass
public class JwtCookieUtils {

    public static void setAccessCookie(HttpServletResponse res,
                                       CookieProperties cp,
                                       String token,
                                       int maxAgeSeconds) {
        add(res, build("ACCESS_TOKEN", token, cp, "/", true, maxAgeSeconds));
    }

    public static void setRefreshCookie(HttpServletResponse res,
                                        CookieProperties cp,
                                        String token,
                                        int maxAgeSeconds) {
        add(res, build("REFRESH_TOKEN", token, cp, "/", true, maxAgeSeconds));
    }

    public static void setLastUsernameCookie(HttpServletResponse res,
                                             CookieProperties cp,
                                             String username) {
        add(res, build("LAST_USERNAME", username == null ? "" : username, cp, "/login", false, 10 * 60));
    }

    public static void clear(HttpServletResponse res, CookieProperties cp) {
        add(res, clearCookie("ACCESS_TOKEN", cp, "/"));
        add(res, clearCookie("REFRESH_TOKEN", cp, "/"));
        add(res, clearCookie("XSRF-TOKEN", cp, "/"));
        add(res, clearCookie("LAST_USERNAME", cp, "/login"));
    }

    private static ResponseCookie build(String name,
                                        String value,
                                        CookieProperties cp,
                                        String path,
                                        boolean httpOnly,
                                        int maxAgeSeconds) {
        var b = ResponseCookie.from(name, value)
                .path(path)
                .httpOnly(httpOnly)
                .secure(cp.isSecure())
                .sameSite(cp.getSameSite())
                .maxAge(maxAgeSeconds);

        if (cp.getDomain() != null && !cp.getDomain().isBlank()) {
            b.domain(cp.getDomain());
        }
        return b.build();
    }

    private static ResponseCookie clearCookie(String name, CookieProperties cp, String path) {
        var b = ResponseCookie.from(name, "")
                .path(path)
                .httpOnly(true)
                .secure(cp.isSecure())
                .sameSite(cp.getSameSite())
                .maxAge(0);

        if (cp.getDomain() != null && !cp.getDomain().isBlank()) {
            b.domain(cp.getDomain());
        }
        return b.build();
    }

    private static void add(HttpServletResponse res, ResponseCookie cookie) {
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
