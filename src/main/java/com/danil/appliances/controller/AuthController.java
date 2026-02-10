package com.danil.appliances.controller;

import com.danil.appliances.dto.account.RegisterDto;
import com.danil.appliances.security.IpUtils;
import com.danil.appliances.security.jwt.*;
import com.danil.appliances.service.AuthService;
import com.danil.appliances.service.impl.LoginRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final LoginRateLimitService rateLimitService;

    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final RefreshTokenService refreshTokenService;

    private final JwtCookieResolver cookieResolver;
    private final CookieProperties cookieProps;


    @GetMapping("/login")
    public String login(HttpServletRequest req, Model model) {
        this.cookieResolver.resolve(req, "LAST_USERNAME")
                .ifPresent(v -> model.addAttribute("lastUsername", v));
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest req,
                          HttpServletResponse res) {

        String email = username.trim().toLowerCase();
        String ip = IpUtils.getClientIp(req);

        this.rateLimitService.assertNotLocked(email, ip);

        try {
            Authentication auth = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            this.rateLimitService.onSuccess(email, ip);

            UserDetails user = this.userDetailsService.loadUserByUsername(email);

            String access = this.jwtService.generateAccessToken(user);
            String refresh = this.jwtService.generateRefreshToken(user);

            this.refreshTokenService.store(email, refresh);

            JwtCookieUtils.setAccessCookie(res, this.cookieProps, access, (int) this.jwtProps.getAccessTtl().toSeconds());
            JwtCookieUtils.setRefreshCookie(res, this.cookieProps, refresh, (int) this.jwtProps.getRefreshTtl().toSeconds());
            JwtCookieUtils.setLastUsernameCookie(res, this.cookieProps, "");


            return "redirect:/";

        } catch (DisabledException ex) {
            this.rateLimitService.onFailure(email, ip);
            JwtCookieUtils.setLastUsernameCookie(res, this.cookieProps, email);
            return "redirect:/login?error=disabled";
        } catch (LockedException ex) {
            this.rateLimitService.onFailure(email, ip);
            JwtCookieUtils.setLastUsernameCookie(res, this.cookieProps, email);
            return "redirect:/login?error=locked";
        } catch (BadCredentialsException ex) {
            this.rateLimitService.onFailure(email, ip);
            JwtCookieUtils.setLastUsernameCookie(res, this.cookieProps, email);
            return "redirect:/login?error=bad";
        }
    }


    @PostMapping("/auth/refresh")
    public String refresh(HttpServletRequest req, HttpServletResponse res) {
        String refresh = this.cookieResolver.resolve(req, jwtProps.getRefreshCookie()).orElse(null);

        if (refresh == null) {
            JwtCookieUtils.clear(res, this.cookieProps);
            return "redirect:/login?error=refresh_missing";
        }

        try {
            if (!this.jwtService.isValid(refresh, JwtType.REFRESH)) {
                JwtCookieUtils.clear(res, this.cookieProps);
                return "redirect:/login?error=refresh_invalid";
            }

            String username = this.jwtService.extractUsername(refresh);
            UserDetails user = this.userDetailsService.loadUserByUsername(username);

            var pair = this.refreshTokenService.rotate(refresh, user);

            JwtCookieUtils.setAccessCookie(res, this.cookieProps, pair.accessToken(), (int) jwtProps.getAccessTtl().toSeconds());
            JwtCookieUtils.setRefreshCookie(res, this.cookieProps, pair.refreshToken(), (int) jwtProps.getRefreshTtl().toSeconds());

            return "redirect:/";

        } catch (Exception ex) {
            JwtCookieUtils.clear(res, this.cookieProps);
            return "redirect:/login?error=refresh_invalid";
        }
    }


    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("register") RegisterDto dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.register", bindingResult);
            redirectAttributes.addFlashAttribute("register", dto);
            return "redirect:/register";
        }

        this.authService.registerClient(dto);
        redirectAttributes.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/login";
    }
}
