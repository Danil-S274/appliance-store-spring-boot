package com.danil.appliances.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final JwtCookieResolver cookieResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            this.cookieResolver
                    .resolve(request, this.jwtProperties.getAccessCookie())
                    .filter(token -> this.jwtService.isValid(token, JwtType.ACCESS))
                    .map(this.jwtService::toAuthentication)
                    .ifPresent(auth ->
                            SecurityContextHolder.getContext().setAuthentication(auth)
                    );
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getServletPath();
        return p.equals("/login")
                || p.equals("/register")
                || p.equals("/auth/refresh")
                || p.equals("/logout");
    }

}
