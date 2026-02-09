package com.danil.appliances.service.impl;

import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.Client;
import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.model.Orders;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.OrdersRepository;
import com.danil.appliances.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrdersRepository ordersRepository;
    private final ClientRepository clientRepository;

    @Override
    public List<Orders> listForEmployee() {
        return this.ordersRepository.findAll();
    }

    @Override
    public List<Orders> listForClient(String clientEmail) {
        Client client = this.clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(clientEmail)));

        return this.ordersRepository.findByClientId(client.getId());
    }

    @Override
    public Orders getDetailsForEmployee(Long orderId) {
        return this.ordersRepository.findWithRowsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: %d".formatted(orderId)));
    }

    @Override
    public Orders getDetailsForClient(Long orderId, String clientEmail) {
        Orders order = this.ordersRepository.findWithRowsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: %d".formatted(orderId)));

        Client client = this.clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(clientEmail)));

        if (!order.getClient().getEmail().equals(client.getEmail())) {
            throw new AccessDeniedException("Forbidden");
        }

        return order;
    }

    @Override
    public Orders getDraftForClient(String clientEmail) {
        Client client = this.clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(clientEmail)));

        return this.ordersRepository.findByClientIdAndOrderStatus(client.getId(), OrderStatus.DRAFT)
                .flatMap(d -> this.ordersRepository.findWithRowsById(d.getId()))
                .orElseGet(() -> {
                    Orders draft = new Orders();
                    draft.setClient(client);
                    draft.setOrderStatus(OrderStatus.DRAFT);
                    return draft;
                });
    }

    @Override
    public Orders getOrderForRead(Long orderId, Authentication auth) {
        Orders order = this.ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: %d".formatted(orderId)));

        boolean isEmployee = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_EMPLOYEE"));

        if(isEmployee) return order;

        Client client = this.clientRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(auth.getName())));

        if(!order.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("Forbidden");
        }

        return order;
    }
}
