package com.danil.appliances.controller;

import com.danil.appliances.dto.account.CheckoutRequestDto;
import com.danil.appliances.mapper.OrderMapper;
import com.danil.appliances.model.Orders;
import com.danil.appliances.service.AccountService;
import com.danil.appliances.service.OrderCommandService;
import com.danil.appliances.service.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    private final AccountService accountService;

    private final OrderMapper orderMapper;

    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        Orders draft = this.orderQueryService.getDraftForClient(auth.getName());
        model.addAttribute("cart", this.orderMapper.toDetailsDto(draft));
        return "cart/cart";
    }

    @PostMapping("/items")
    public String addItem(Authentication auth,
                          @RequestParam Long applianceId,
                          @RequestParam long quantity,
                          RedirectAttributes ra) {

        this.orderCommandService.addItemToDraft(auth.getName(), applianceId, quantity);
        ra.addFlashAttribute("success", "Added to cart");
        return "redirect:/cart";
    }

    @PostMapping("/rows/{rowId}/update")
    public String updateItem(Authentication auth,
                             @PathVariable Long rowId,
                             @RequestParam long quantity,
                             RedirectAttributes ra) {

        this.orderCommandService.updateItem(auth.getName(), rowId, quantity);
        ra.addFlashAttribute("success", "Updated");
        return "redirect:/cart";
    }

    @PostMapping("/rows/{rowId}/delete")
    public String deleteItem(Authentication auth,
                             @PathVariable Long rowId,
                             RedirectAttributes ra) {

        this.orderCommandService.deleteItem(auth.getName(), rowId);
        ra.addFlashAttribute("success", "Deleted");
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Authentication auth, Model model) {
        Orders draft = this.orderQueryService.getDraftForClient(auth.getName());

        BigDecimal total = draft.getAmount();
        BigDecimal balance = this.accountService.getBalance(auth.getName());
        BigDecimal deficit = total.subtract(balance);
        if (deficit.compareTo(BigDecimal.ZERO) < 0) {
            deficit = BigDecimal.ZERO;
        }

        model.addAttribute("cart", this.orderMapper.toDetailsDto(draft));
        model.addAttribute("balance", balance);
        model.addAttribute("deficit", deficit);

        if (!model.containsAttribute("checkout")) {
            CheckoutRequestDto dto = new CheckoutRequestDto();
            dto.setDeliveryName(draft.getDeliveryName());
            dto.setDeliveryPhone(draft.getDeliveryPhone());
            dto.setDeliveryAddress(draft.getDeliveryAddress());
            dto.setDeliveryComment(draft.getDeliveryComment());

            if (deficit.compareTo(BigDecimal.ZERO) > 0) {
                dto.setTopUpAmount(deficit);
            }

            model.addAttribute("checkout", dto);
        }

        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("checkout") CheckoutRequestDto dto,
                           BindingResult bindingResult,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        Orders draft = this.orderQueryService.getDraftForClient(auth.getName());

        BigDecimal total = draft.getAmount();
        BigDecimal balance = this.accountService.getBalance(auth.getName());
        BigDecimal deficit = total.subtract(balance);

        if (deficit.compareTo(BigDecimal.ZERO) < 0) deficit = BigDecimal.ZERO;

        if (deficit.compareTo(BigDecimal.ZERO) > 0) {
            if (dto.getTopUpAmount() == null) {
                bindingResult.rejectValue("topUpAmount", "topup.required",
                        "Top up is required because balance is not enough");
            } else if (dto.getTopUpAmount().compareTo(deficit) < 0) {
                bindingResult.rejectValue("topUpAmount", "topup.tooSmall",
                        "Top up must be at least " + deficit);
            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.checkout", bindingResult);
            redirectAttributes.addFlashAttribute("checkout", dto);
            return "redirect:/cart/checkout";
        }

        this.orderCommandService.updateDraftDelivery(auth.getName(), dto);

        if (dto.getTopUpAmount() != null && dto.getTopUpAmount().compareTo(BigDecimal.ZERO) > 0) {
            this.accountService.topUpBalance(auth.getName(), dto.getTopUpAmount());
        }

        Orders created = this.orderCommandService.checkout(auth.getName());
        redirectAttributes.addFlashAttribute("success", "Order created");
        return "redirect:/orders/" + created.getId();
    }
}
