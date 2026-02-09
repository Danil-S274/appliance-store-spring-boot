package com.danil.appliances.controller.admin;

import com.danil.appliances.dto.ManufacturerDto;
import com.danil.appliances.mapper.ManufacturerMapper;
import com.danil.appliances.model.Manufacturer;
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
@RequestMapping("/admin/manufacturers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ManufacturerAdminController {

    private final ManufacturerService manufacturerService;

    private final ManufacturerMapper manufacturerMapper;

    @GetMapping
    public String list(Model model) {
        List<ManufacturerDto> list = this.manufacturerService.findAll().stream()
                .map(this.manufacturerMapper::toDto)
                .toList();
        model.addAttribute("manufacturers", list);
        return "admin/manufacturers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("manufacturer")) {
            model.addAttribute("manufacturer", new ManufacturerDto());
        }
        model.addAttribute("mode", "create");
        return "admin/manufacturers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("manufacturer") ManufacturerDto dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/manufacturers/form";
        }

        try {
            this.manufacturerService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Manufacturer created");
            return "redirect:/admin/manufacturers";
        } catch (Exception ex) {
            bindingResult.rejectValue("name", "manufacturer.name", ex.getMessage());
            model.addAttribute("mode", "create");
            return "admin/manufacturers/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Manufacturer manufacturer = this.manufacturerService.findById(id);
        model.addAttribute("manufacturer", this.manufacturerMapper.toDto(manufacturer));;
        model.addAttribute("mode", "edit");
        return "admin/manufacturers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("manufacturer") ManufacturerDto dto,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "admin/manufacturers/form";
        }

        try {
            this.manufacturerService.update(id, dto);
            ra.addFlashAttribute("success", "Manufacturer updated");
            return "redirect:/admin/manufacturers";
        } catch (Exception ex) {
            br.rejectValue("name", "manufacturer.name", ex.getMessage());
            model.addAttribute("mode", "edit");
            return "admin/manufacturers/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            this.manufacturerService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Manufacturer deleted");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/manufacturers";
    }
}
