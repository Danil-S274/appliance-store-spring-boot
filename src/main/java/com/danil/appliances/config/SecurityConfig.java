package com.danil.appliances.config;

import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.EmployeeRepository;
import com.danil.appliances.security.LoginPreCheckFilter;
import com.danil.appliances.security.jwt.*;
import com.danil.appliances.security.oauth.CustomOidcUserService;
import com.danil.appliances.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, CookieProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final CookieProperties cookieProps;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> this.employeeRepository.findByEmail(username)
                .<UserDetails>map(employee -> User.withUsername(employee.getEmail())
                        .password(employee.getPassword())
                        .authorities("ROLE_EMPLOYEE")
                        .disabled(!employee.isEnabled())
                        .build())
                .or(() -> this.clientRepository.findByEmail(username)
                        .map(client -> User.withUsername(client.getEmail())
                                .password(client.getPassword())
                                .authorities("ROLE_CLIENT")
                                .disabled(!client.isEnabled())
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: %s".formatted(username)));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(encoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                           JwtProperties jwtProperties,
                                                           JwtCookieResolver resolver) {
        return new JwtAuthenticationFilter(jwtService, jwtProperties, resolver);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain staticChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PathRequest.toStaticResources().atCommonLocations())
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http,
                                        JwtAuthenticationFilter jwtFilter,
                                        LoginPreCheckFilter loginPreCheckFilter,
                                        CustomOidcUserService customOidcUserService,
                                        OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {

        http
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(securitySecurityContextConfigurer ->
                        securitySecurityContextConfigurer.securityContextRepository(new NullSecurityContextRepository()))
                .requestCache(requestCacheConfigurer ->
                        requestCacheConfigurer.requestCache(new NullRequestCache()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers("/login", "/auth/refresh", "/logout")
                )
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers(
                                "/", "/login", "/register", "/auth/refresh",
                                "/catalog/**",
                                "/oauth2/**", "/login/oauth2/**",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()

                        // client
                        .requestMatchers("/cart/**").hasRole("CLIENT")
                        .requestMatchers("/account/client/**").hasRole("CLIENT")

                        // employee
                        .requestMatchers("/account/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/admin/**").hasRole("EMPLOYEE")

                        // authenticated
                        .requestMatchers("/orders/**").authenticated()
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter, CsrfFilter.class)
                .addFilterBefore(loginPreCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) ->
                                res.sendRedirect("/login"))
                        .accessDeniedHandler((req, res, ex) ->
                                res.sendRedirect("/login"))
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            JwtCookieUtils.clear(res, this.cookieProps);
                            res.sendRedirect("/");
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
