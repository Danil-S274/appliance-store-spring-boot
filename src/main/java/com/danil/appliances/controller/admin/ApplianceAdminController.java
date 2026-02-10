package com.danil.appliances.controller.admin;

import com.danil.appliances.dto.appliance.ApplianceDto;
import com.danil.appliances.dto.appliance.ManufacturerDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.mapper.ApplianceMapper;
import com.danil.appliances.mapper.ManufacturerMapper;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Category;
import com.danil.appliances.model.PowerType;
import com.danil.appliances.service.ApplianceService;
import com.danil.appliances.service.ManufacturerService;
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
@RequestMapping("/admin/appliances")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ApplianceAdminController {

    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;

    private final ApplianceMapper applianceMapper;
    private final ManufacturerMapper manufacturerMapper;

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    @ModelAttribute("powerTypes")
    public PowerType[] powerTypes() {
        return PowerType.values();
    }

    @ModelAttribute("manufacturers")
    public List<ManufacturerDto> manufacturers() {
        return this.manufacturerService.findAll().stream()
                .map(this.manufacturerMapper::toDto)
                .toList();
    }

    @GetMapping
    public String list(Model model) {
        List<ApplianceDto> list = this.applianceService.findAll().stream()
                .map(this.applianceMapper::toDto)
                .toList();
        model.addAttribute("appliances", list);
        return "admin/appliances/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("appliance")) {
            model.addAttribute("appliance", new ApplianceDto());
        }
        model.addAttribute("mode", "create");
        return "admin/appliances/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("appliance") ApplianceDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/appliances/form";
        }

        try {
            this.applianceService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Appliance created");
            return "redirect:/admin/appliances";
        } catch (Exception e) {
            bindingResult.reject("appliance", e.getMessage());
            model.addAttribute("mode", "create");
            return "admin/appliances/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Appliance appliance = this.applianceService.findById(id);
        model.addAttribute("appliance", this.applianceMapper.toDto(appliance));
        model.addAttribute("mode", "edit");
        return "admin/appliances/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("appliance") ApplianceDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "admin/appliances/form";
        }

        try {
            dto.setId(id);
            this.applianceService.update(dto);
            redirectAttributes.addFlashAttribute("success", "Appliance updated");
            return "redirect:/admin/appliances";
        } catch (Exception e) {
            bindingResult.reject("appliance", e.getMessage());
            model.addAttribute("mode", "edit");
            return "admin/appliances/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            this.applianceService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Appliance deleted");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/appliances";
    }

}


