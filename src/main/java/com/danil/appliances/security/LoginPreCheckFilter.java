package com.danil.appliances.security;

import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.service.impl.LoginRateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginPreCheckFilter extends OncePerRequestFilter {

    private final LoginRateLimitService rateLimitService;
    private final AntPathRequestMatcher matcher =
            new AntPathRequestMatcher("/login", "POST");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (this.matcher.matches(request)) {
            String username = request.getParameter("username");
            String ip = IpUtils.getClientIp(request);
            try {
                this.rateLimitService.assertNotLocked(username, ip);
            } catch (BusinessException ex) {
                response.sendRedirect("/login?locked");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

