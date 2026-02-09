package com.danil.appliances.service.impl;

import com.danil.appliances.dto.RegisterDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.Client;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.EmployeeRepository;
import com.danil.appliances.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Client registerClient(RegisterDto dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (this.employeeRepository.existsByEmail(email) || this.clientRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists: %s".formatted(email));
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        String cardNorm = normalizeCard(dto.getCardNumber());
        if (cardNorm.length() < 12 || cardNorm.length() > 19) {
            throw new BusinessException("Invalid card number");
        }

        Client client = new Client();
        client.setName(dto.getName().trim());
        client.setEmail(email);
        client.setPassword(this.passwordEncoder.encode(dto.getPassword()));

        client.setCardLast4(cardNorm.substring(cardNorm.length() - 4));
        client.setCardHash(sha256Hex(cardNorm));

        if (client.getBalance() == null) {
            client.setBalance(new java.math.BigDecimal("100.00"));
        }

        Client saved = this.clientRepository.save(client);
        log.info("Registered new client email={}", email);
        return saved;
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizeCard(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }
}
