package com.danil.appliances.controller.admin;

import com.danil.appliances.dto.user.ClientDto;
import com.danil.appliances.dto.user.ClientUpdateDto;
import com.danil.appliances.mapper.ClientMapper;
import com.danil.appliances.model.Client;
import com.danil.appliances.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ClientAdminController {

    private final ClientService clientService;

    private final ClientMapper clientMapper;

    @GetMapping
    public String list(Model model) {
        List<ClientDto> list = this.clientService.findAll().stream()
                .map(this.clientMapper::toDto)
                .toList();
        model.addAttribute("clients", list);
        return "admin/clients/list";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Client client = this.clientService.findById(id);

        ClientUpdateDto dto = new ClientUpdateDto();
        dto.setName(client.getName());

        model.addAttribute("clientId", id);
        model.addAttribute("email", client.getEmail());
        model.addAttribute("enabled", client.isEnabled());
        model.addAttribute("balance", client.getBalance());
        model.addAttribute("cardLast4", client.getCardLast4());
        model.addAttribute("client", dto);
        return "admin/clients/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("client") ClientUpdateDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clientId", id);
            return "admin/clients/form";
        }
        this.clientService.update(id, dto);
        redirectAttributes.addFlashAttribute("success", "Client updated");
        return "redirect:/admin/clients/" + id + "/edit";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam boolean enabled, RedirectAttributes ra) {
        this.clientService.setEnabled(id, enabled);
        ra.addFlashAttribute("success", enabled ? "Client enabled" : "Client disabled");
        return "redirect:/admin/clients";
    }
}

