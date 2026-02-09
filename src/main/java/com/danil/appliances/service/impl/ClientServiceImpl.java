package com.danil.appliances.service.impl;

import com.danil.appliances.dto.ClientCreateDto;
import com.danil.appliances.dto.ClientUpdateDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.ClientMapper;
import com.danil.appliances.model.Client;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return this.clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return this.clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: id=%d".formatted(id)));
    }

    @Override
    public Client create(ClientCreateDto dto) {
        if (this.clientRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Client with email already exists: %s".formatted(dto.getEmail()));
        }

        Client client = this.clientMapper.toEntity(dto);
        client.setEmail(dto.getEmail());
        client.setName(dto.getName().trim());
        client.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        this.applyCard(client, dto.getCardNumber());
        Client saved = this.clientRepository.save(client);
        log.info("Created Client id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public Client update(Long id, ClientUpdateDto dto) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: %d".formatted(id)));

        this.clientMapper.updateEntity(dto, client);
        client.setName(dto.getName().trim());
        Client saved = this.clientRepository.save(client);
        log.info("Updated Client id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }


    @Override
    public void delete(Long id) {
        if (!this.clientRepository.existsById(id)) {
            throw new NotFoundException("Client not found: %s".formatted(id));
        }
        this.clientRepository.deleteById(id);
        log.info("Deleted client id={}", id);
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: %d".formatted(id)));
        client.setEnabled(enabled);
        this.clientRepository.save(client);
        log.info("Client id={} enabled={}", id, enabled);
    }

    // Helper

    private void applyCard(Client client, String cardNumberRaw) {
        if (cardNumberRaw == null) {
            throw new BusinessException("Card number is required");
        }

        String card = cardNumberRaw.replaceAll("\\s+", "");
        if (!card.matches("^[0-9]{13,19}$")) {
            throw new BusinessException("Card number must be 13-19 digits");
        }

        String last4 = card.substring(card.length() - 4);
        client.setCardLast4(last4);
        client.setCardHash(sha256Hex(card));
    }

    private String sha256Hex(String value) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash card", e);
        }
    }
}
