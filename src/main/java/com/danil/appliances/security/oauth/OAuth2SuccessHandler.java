package com.danil.appliances.security.oauth;

import com.danil.appliances.security.jwt.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final CookieProperties cookieProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = extractEmail(authentication);

        if (email == null || email.isBlank()) {
            response.sendRedirect("/login?error=oauth_email_missing");
            return;
        }

        email = email.trim().toLowerCase();
        UserDetails user = this.userDetailsService.loadUserByUsername(email);

        String access = this.jwtService.generateAccessToken(user);
        String refresh = this.jwtService.generateRefreshToken(user);

        this.refreshTokenService.store(email, refresh);

        JwtCookieUtils.setAccessCookie(response, this.cookieProperties, access,
                (int) this.jwtProps.getAccessTtl().toSeconds());

        JwtCookieUtils.setRefreshCookie(response, this.cookieProperties, refresh,
                (int) this.jwtProps.getRefreshTtl().toSeconds());

        response.sendRedirect("/");
    }

    private static String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }

        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("email");
        }

        return null;
    }
}
