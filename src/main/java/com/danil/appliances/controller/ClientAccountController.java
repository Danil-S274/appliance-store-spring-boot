package com.danil.appliances.controller;

import com.danil.appliances.dto.AccountUpdateDto;
import com.danil.appliances.dto.ChangePasswordDto;
import com.danil.appliances.dto.TopUpBalanceDto;
import com.danil.appliances.dto.UpdateCardDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.Client;
import com.danil.appliances.security.jwt.CookieProperties;
import com.danil.appliances.security.jwt.JwtCookieUtils;
import com.danil.appliances.security.jwt.RefreshTokenService;
import com.danil.appliances.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account/client")
@PreAuthorize("hasRole('CLIENT')")
public class ClientAccountController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;

    private final CookieProperties cookieProperties;

    @GetMapping
    public String page(Authentication auth, Model model) {
        Client client = this.accountService.getClient(auth.getName());

        if (!model.containsAttribute("profile")) {
            AccountUpdateDto dto = new AccountUpdateDto();
            dto.setName(client.getName());
            model.addAttribute("profile", dto);
        }
        if (!model.containsAttribute("card")) {
            model.addAttribute("card", new UpdateCardDto());
        }
        if (!model.containsAttribute("topup")) {
            model.addAttribute("topup", new TopUpBalanceDto());
        }
        if (!model.containsAttribute("pwd")) {
            model.addAttribute("pwd", new ChangePasswordDto());
        }

        model.addAttribute("balance", this.accountService.getBalance(auth.getName()));
        model.addAttribute("cardLast4", client.getCardLast4());
        model.addAttribute("email", client.getEmail());
        return "account/client/clientAccount";
    }


    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profile") AccountUpdateDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Authentication auth) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profile", bindingResult);
            redirectAttributes.addFlashAttribute("profile", dto);
            return "redirect:/account/client";
        }
        this.accountService.updateProfile(auth.getName(), dto);
        redirectAttributes.addFlashAttribute("success", "Profile updated");
        return "redirect:/account/client";
    }

    @PostMapping("/card")
    public String updatedCard(@Valid @ModelAttribute("card") UpdateCardDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Authentication auth) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.card", bindingResult);
            redirectAttributes.addFlashAttribute("card", dto);
            return "redirect:/account/client";
        }
        this.accountService.updateCard(auth.getName(), dto);
        redirectAttributes.addFlashAttribute("success", "Card updated");
        return "redirect:/account/client";
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        if (!model.containsAttribute("pwd")) {
            model.addAttribute("pwd", new ChangePasswordDto());
        }
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("pwd") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            bindingResult.rejectValue("confirmNewPassword", "password.mismatch", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.pwd", bindingResult);
            redirectAttributes.addFlashAttribute("pwd", dto);
            return "redirect:/account/client";
        }

        this.accountService.changeClientPassword(auth.getName(), dto);
        redirectAttributes.addFlashAttribute("success", "Password changed");
        return "redirect:/account/client";
    }

    @PostMapping("/balance")
    public String topUp(@Valid @ModelAttribute("topup") TopUpBalanceDto topUpBalanceDto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Authentication auth) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.topup", bindingResult);
            redirectAttributes.addFlashAttribute("topup", topUpBalanceDto);
            return "redirect:/account/client";
        }
        this.accountService.topUpBalance(auth.getName(), topUpBalanceDto.getAmount());
        redirectAttributes.addFlashAttribute("success", "Balance topped up");
        return "redirect:/account/client";
    }

    @PostMapping("/delete")
    public String deleteAccount(Authentication auth,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        try {
            String email = auth.getName();
            this.accountService.deleteAccount(email);
            this.refreshTokenService.revokeAll(email);

            JwtCookieUtils.clear(response,this.cookieProperties);

            new SecurityContextLogoutHandler().logout(request, response, auth);

            redirectAttributes.addFlashAttribute("success", "Account deleted");
            return "redirect:/";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account/client?tab=danger";
        }
    }


}
