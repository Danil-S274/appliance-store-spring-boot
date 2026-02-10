package com.danil.appliances.security.oauth;

import com.danil.appliances.model.AuthProvider;
import com.danil.appliances.model.Client;
import com.danil.appliances.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google did not return email");
        }

        email = email.trim().toLowerCase();
        String displayName = (name == null || name.isBlank()) ? "Google User" : name.trim();

        String finalEmail = email;
        this.clientRepository.findByEmail(email).orElseGet(() -> {
            Client c = new Client();
            c.setEmail(finalEmail);
            c.setName(displayName);
            c.setEnabled(true);
            c.setProvider(AuthProvider.GOOGLE);
            c.setPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));
            c.setBalance(BigDecimal.ZERO);
            return this.clientRepository.saveAndFlush(c);
        });

        return oidcUser;
    }
}
