package com.danil.appliances.security;

import com.danil.appliances.service.impl.LoginRateLimitService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginRateLimitService rateLimitService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String ip = IpUtils.getClientIp(request);

        this.rateLimitService.onSuccess(username, ip);

        request.getSession(false).removeAttribute("LAST_USERNAME");

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
