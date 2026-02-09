package com.danil.appliances.controller;

import com.danil.appliances.mapper.OrderMapper;
import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.service.OrderCommandService;
import com.danil.appliances.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    private final OrderMapper orderMapper;

    @GetMapping
    public String list(Authentication auth, Model model) {
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        var orders = isEmployee
                ? this.orderQueryService.listForEmployee()
                : this.orderQueryService.listForClient(auth.getName()).stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.DRAFT)
                .toList();

        model.addAttribute("orders", orders.stream().map(this.orderMapper::toListItemDto).toList());
        return "order/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Authentication auth, Model model) {
        boolean employee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        var order = employee
                ? this.orderQueryService.getDetailsForEmployee(id)
                : this.orderQueryService.getDetailsForClient(id, auth.getName());

        model.addAttribute("order", this.orderMapper.toDetailsDto(order));
        model.addAttribute("isEmployee", employee);
        return "order/details";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        this.orderCommandService.approve(id, auth);
        ra.addFlashAttribute("success", "Approved");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        this.orderCommandService.cancel(id, auth);
        ra.addFlashAttribute("success", "Canceled");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        this.orderCommandService.deleteOrder(id, auth);
        ra.addFlashAttribute("success", "Deleted");
        return "redirect:/orders";
    }
}

