package com.danil.appliances.service.impl;

import com.danil.appliances.dto.AccountUpdateDto;
import com.danil.appliances.dto.ChangePasswordDto;
import com.danil.appliances.dto.UpdateCardDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.Client;
import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.OrdersRepository;
import com.danil.appliances.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final OrdersRepository ordersRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Client getClient(String email) {
        return this.clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(email)));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String email) {
        Client client = this.getClient(email);
        return client.getBalance() == null ? BigDecimal.ZERO : client.getBalance();
    }

    @Override
    public Client updateProfile(String email, AccountUpdateDto dto) {
        Client client = this.getClient(email);
        client.setName(dto.getName().trim());
        Client saved = this.clientRepository.save(client);
        log.info("Updated account for client={}", email);
        return saved;
    }

    @Override
    public Client updateCard(String email, UpdateCardDto dto) {
        Client client = this.getClient(email);

        CardData cardData = normalizeAndHashCard(dto.getCardNumber());
        client.setCardLast4(cardData.last4);
        client.setCardHash(cardData.hashHex);

        Client saved = this.clientRepository.save(client);
        log.info("Updated card for client={} last4={}", email, saved.getCardLast4());
        return saved;
    }

    @Override
    public void changeClientPassword(String email, ChangePasswordDto dto) {
        Client client = this.getClient(email);

        if (!this.passwordEncoder.matches(dto.getCurrentPassword(), client.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        client.setPassword(this.passwordEncoder.encode(dto.getNewPassword()));
        this.clientRepository.save(client);
        log.info("Changed password for client={}", email);
    }

    @Override
    public BigDecimal topUpBalance(String email, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Top up amount must be > 0");
        }

        Client client = this.getClient(email);
        BigDecimal current = client.getBalance() == null ? BigDecimal.ZERO : client.getBalance();
        BigDecimal updated = current.add(amount);
        client.setBalance(updated);
        this.clientRepository.save(client);
        log.info("Top up balance client={} +{}", email, amount);
        return updated;
    }

    @Override
    public void deleteAccount(String email) {
        Client client = this.getClient(email);

        boolean hasNew = this.ordersRepository.existsByClientIdAndOrderStatus(client.getId(), OrderStatus.NEW);
        if (hasNew) {
            throw new BusinessException("You cannot delete account while you have NEW orders");
        }

        this.ordersRepository.findByClientIdAndOrderStatus(client.getId(), OrderStatus.DRAFT)
                .ifPresent(this.ordersRepository::delete);

        client.setEnabled(false);
        this.clientRepository.save(client);
        log.info("Client deactivated email={}", email);
    }

    // Helper

    private CardData normalizeAndHashCard(String raw) {
        if (raw == null) throw new BusinessException("Card number is required");

        String digits = raw.replaceAll("\\s+", "");
        if (!digits.matches("^[0-9]{13,19}$")) {
            throw new BusinessException("Card number must be 13-19 digits");
        }

        String last4 = digits.substring(digits.length() - 4);
        String hash = sha256Hex(digits);

        return new CardData(last4, hash);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash card", e);
        }
    }

    private record CardData(String last4, String hashHex) {}
}


