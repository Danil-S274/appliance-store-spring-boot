    package com.danil.appliances.security;

    import com.danil.appliances.service.impl.LoginRateLimitService;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.authentication.DisabledException;
    import org.springframework.security.authentication.LockedException;
    import org.springframework.security.core.AuthenticationException;
    import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
    import org.springframework.stereotype.Component;

    import java.io.IOException;

    @Component
    @RequiredArgsConstructor
    public class RateLimitAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

        private final LoginRateLimitService rateLimitService;

        @Override
        public void onAuthenticationFailure(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException exception)
                throws IOException, ServletException {

            String username = request.getParameter("username");
            String ip = IpUtils.getClientIp(request);

            this.rateLimitService.onFailure(username, ip);
            request.getSession(true).setAttribute("LAST_USERNAME", username);

            String error = "bad";
            if (exception instanceof DisabledException) {
                error = "disabled";
            } else if (exception instanceof LockedException) {
                error = "locked";
            }

            setDefaultFailureUrl("/login?error=" + error);
            super.onAuthenticationFailure(request, response, exception);
        }

    }

