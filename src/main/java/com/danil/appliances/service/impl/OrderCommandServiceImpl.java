package com.danil.appliances.service.impl;

import com.danil.appliances.dto.CheckoutRequestDto;
import com.danil.appliances.dto.OrderCreateDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.*;
import com.danil.appliances.repository.*;
import com.danil.appliances.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrdersRepository ordersRepository;
    private final OrderRowRepository orderRowRepository;

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final ApplianceRepository applianceRepository;

    @Override
    public Orders createForEmployee(OrderCreateDto dto) {
        Client client = this.clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found: %d".formatted(dto.getClientId())));
        Employee employee = this.employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found: %d".formatted(dto.getEmployeeId())));

        Orders order = new Orders();
        order.setEmployee(employee);
        order.setClient(client);
        order.setOrderStatus(OrderStatus.NEW);

        Orders saved = this.ordersRepository.save(order);
        log.info("Employee created order id={}, clientId={}, employeeId={}", saved.getId(), client.getId(), employee.getId());
        return saved;
    }

    @Override
    public Orders getOrCreateDraft(String clientEmail) {
        Client client = this.reqireClient(clientEmail);

        return ordersRepository.findByClientIdAndOrderStatus(client.getId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    Orders draft = new Orders();
                    draft.setClient(client);
                    draft.setEmployee(null);
                    draft.setOrderStatus(OrderStatus.DRAFT);
                    Orders saved = this.ordersRepository.save(draft);
                    log.info("Created draft order id={} for client={}", saved.getId(), clientEmail);
                    return saved;
                });
    }

    @Override
    public void addItemToDraft(String clientEmail, Long applianceId, long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        Orders finalDraft = this.getOrCreateDraft(clientEmail);
        Orders draft = this.ordersRepository.findWithRowsById(finalDraft.getId())
                .orElseThrow(() -> new NotFoundException("Draft not found: %d".formatted(finalDraft.getId())));

        Appliance appliance = this.applianceRepository.findById(applianceId)
                .orElseThrow(() -> new NotFoundException("Appliance not found: %d".formatted(applianceId)));

        OrderRow row = this.orderRowRepository.findByOrdersIdAndApplianceId(draft.getId(), applianceId)
                .orElseGet(() -> {
                    OrderRow orderRow = new OrderRow();
                    orderRow.setOrders(draft);
                    orderRow.setAppliance(appliance);
                    orderRow.setNumber(0L);
                    orderRow.setAmount(BigDecimal.ZERO);
                    return orderRow;
                });

        long newQuantity = row.getNumber() + quantity;
        row.setNumber(newQuantity);
        row.setAmount(appliance.getPrice().multiply(BigDecimal.valueOf(newQuantity)));

        this.orderRowRepository.save(row);
        log.info("Draft id={} add item applianceId={}, qty={}", draft.getId(), applianceId, quantity);
    }

    @Override
    public void updateItem(String clientEmail, Long rowId, long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        Orders draft = this.requireDraft(clientEmail);
        this.ensureDraftEditable(draft);

        OrderRow row = this.orderRowRepository.findByIdAndOrdersId(rowId, draft.getId())
                .orElseThrow(() -> new NotFoundException("OrderRow not found: %d".formatted(rowId)));

        if (!row.getOrders().getId().equals(draft.getId())) {
            throw new AccessDeniedException("Forbidden");
        }

        row.setNumber(quantity);
        row.setAmount(row.getAppliance().getPrice().multiply(BigDecimal.valueOf(quantity)));
        this.orderRowRepository.save(row);
        log.info("Draft id={} updated rowId={}, qty={}", draft.getId(), rowId, quantity);
    }

    @Override
    public void updateDraftDelivery(String clientEmail, CheckoutRequestDto dto) {
        Orders draftRef = this.requireDraft(clientEmail);
        Orders draft = this.ordersRepository.findById(draftRef.getId())
                .orElseThrow(() -> new NotFoundException("Draft not found: %d".formatted(draftRef.getId())));

        ensureDraftEditable(draft);
        draft.setDeliveryName(dto.getDeliveryName().trim());
        draft.setDeliveryPhone(dto.getDeliveryPhone().trim());
        draft.setDeliveryAddress(dto.getDeliveryAddress().trim());
        draft.setDeliveryComment(dto.getDeliveryComment() == null ? null : dto.getDeliveryComment().trim());
        this.ordersRepository.save(draft);
    }

    @Override
    public void deleteItem(String clientEmail, Long rowId) {
        Orders draft = this.requireDraft(clientEmail);
        this.ensureDraftEditable(draft);

        OrderRow row = this.orderRowRepository.findByIdAndOrdersId(rowId, draft.getId())
                .orElseThrow(() -> new NotFoundException("OrderRow not found: %d".formatted(rowId)));

        if (!row.getOrders().getId().equals(draft.getId())) {
            throw new AccessDeniedException("Forbidden");
        }

        this.orderRowRepository.delete(row);
        log.info("Draft id={} deleted rowId={}", draft.getId(), rowId);
    }

    @Override
    public Orders checkout(String clientEmail) {
        Client client = this.reqireClient(clientEmail);

        Orders draftRef = this.requireDraft(clientEmail);
        Orders draft = this.ordersRepository.findWithRowsById(draftRef.getId())
                .orElseThrow(() -> new NotFoundException("Draft not found: %d".formatted(draftRef.getId())));

        this.ensureDraftEditable(draft);

        if (draft.getRows() == null || draft.getRows().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        BigDecimal total = draft.getAmount();
        BigDecimal balance = client.getBalance() == null ? BigDecimal.ZERO : client.getBalance();

        if (balance.compareTo(total) < 0) {
            throw new BusinessException("Not enough funds. Balance=%s, total=%s".formatted(balance, total));
        }

        client.setBalance(balance.subtract(total));
        this.clientRepository.save(client);

        draft.setOrderStatus(OrderStatus.NEW);
        Orders saved = this.ordersRepository.save(draft);

        log.info("Checkout order id={} client={} total={}", saved.getId(), clientEmail, total);
        return saved;
    }


    @Override
    public Orders approve(Long orderId, Authentication auth) {
        Orders order = this.ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: %d".formatted(orderId)));

        if (order.getOrderStatus() != OrderStatus.NEW) {
            throw new BusinessException("Only NEW orders can be approved");
        }

        Employee employee = this.employeeRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("Employee not found: %s".formatted(auth.getName())));

        order.setEmployee(employee);
        order.setOrderStatus(OrderStatus.APPROVED);
        Orders saved = this.ordersRepository.save(order);
        log.info("Approved order id={}", saved.getId());
        return saved;
    }

    @Override
    public Orders cancel(Long orderId, Authentication auth) {
        Orders order = getOrderForRead(orderId, auth);

        if (order.getOrderStatus() == OrderStatus.APPROVED) {
            throw new BusinessException("Approved order cannot be canceled");
        }

        boolean employee = isEmployee(auth);
        if (!employee) {
            if (order.getOrderStatus() != OrderStatus.NEW) {
                throw new AccessDeniedException("Client can cancel only NEW orders");
            }
        }

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            log.info("Order id={} already canceled", orderId);
            return order;
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        this.ordersRepository.save(order);
        log.info("Canceled order id={} by {}", orderId, auth.getName());
        return order;
    }


    @Override
    public void deleteOrder(Long orderId, Authentication auth) {
        Orders order = this.getOrderForRead(orderId, auth);

        boolean employee = this.isEmployee(auth);
        if (!employee) {
            if (order.getOrderStatus() != OrderStatus.DRAFT) {
                throw new AccessDeniedException("Clients can delete only DRAFT orders");
            }
        }

        this.ordersRepository.delete(order);
        log.info("Deleted order id={} by {}", orderId, auth.getName());
    }

    // Helpers

    private Client reqireClient(String email) {
        return this.clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: %s".formatted(email)));
    }

    private Orders requireDraft(String clientEmail) {
        Client client = reqireClient(clientEmail);
        return ordersRepository.findByClientIdAndOrderStatus(client.getId(), OrderStatus.DRAFT)
                .orElseThrow(() -> new NotFoundException("Cart is empty"));
    }

    private void ensureDraftEditable(Orders draft) {
        if (draft.getOrderStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Only DRAFT order can be changed");
        }
    }

    private boolean isEmployee(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    private Orders getOrderForRead(Long orderId, Authentication auth) {
        Orders order = this.ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: %d".formatted(orderId)));

        if (isEmployee(auth)) return order;

        Client client = this.reqireClient(auth.getName());
        if (!order.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("Forbidden");
        }
        return order;
    }
}
