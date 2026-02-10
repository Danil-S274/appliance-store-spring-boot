package com.danil.appliances.controller.admin;

import com.danil.appliances.dto.account.AdminSetPasswordDto;
import com.danil.appliances.dto.user.EmployeeCreateDto;
import com.danil.appliances.dto.user.EmployeeDto;
import com.danil.appliances.dto.user.EmployeeUpdateDto;
import com.danil.appliances.mapper.EmployeeMapper;
import com.danil.appliances.model.Employee;
import com.danil.appliances.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeAdminController {

    private final EmployeeService employeeService;

    private final EmployeeMapper employeeMapper;

    @GetMapping
    public String list(Model model, Authentication auth) {
        List<EmployeeDto> list = this.employeeService.findAll().stream()
                .map(this.employeeMapper::toDto)
                .toList();
        model.addAttribute("employee", list);
        model.addAttribute("currentEmail", auth.getName());
        return "admin/employees/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new EmployeeCreateDto());
        }
        model.addAttribute("mode", "create");
        return "admin/employees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("employee") EmployeeCreateDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/employees/form";
        }
        this.employeeService.create(dto);
        redirectAttributes.addFlashAttribute("success", "Employee created");
        return "redirect:/admin/employees";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = this.employeeService.findById(id);

        EmployeeUpdateDto dto = new EmployeeUpdateDto();
        dto.setName(employee.getName());
        dto.setDepartment(employee.getDepartment());

        if (!model.containsAttribute("pwd")) {
            model.addAttribute("pwd", new AdminSetPasswordDto());
        }
        model.addAttribute("employeeId", id);
        model.addAttribute("email", employee.getEmail());
        model.addAttribute("enabled", employee.isEnabled());
        model.addAttribute("employee", dto);
        model.addAttribute("mode", "edit");
        return "admin/employees/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("employee") EmployeeUpdateDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("employeeId", id);
            return "admin/employees/form";
        }
        this.employeeService.update(id, dto);
        redirectAttributes.addFlashAttribute("success", "Employee updated");
        return "redirect:/admin/employees";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam boolean enabled,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        this.employeeService.setEnabled(id, enabled, auth.getName());
        redirectAttributes.addFlashAttribute("success", enabled ? "Employee enabled" : "Employee disabled");
        return "redirect:/admin/employees";
    }

    @PostMapping("/{id}/password")
    public String setPassword(@PathVariable Long id,
                              @Valid @ModelAttribute("pwd") AdminSetPasswordDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.pwd", bindingResult);
            redirectAttributes.addFlashAttribute("pwd", dto);
            return "redirect:/admin/employees/" + id + "/edit";
        }
        this.employeeService.setPassword(id, dto.getPassword());
        redirectAttributes.addFlashAttribute("success", "Password updated");
        return "redirect:/admin/employees/" + id + "/edit";
    }
}
