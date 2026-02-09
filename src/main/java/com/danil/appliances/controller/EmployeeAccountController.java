package com.danil.appliances.controller;

import com.danil.appliances.dto.ChangePasswordDto;
import com.danil.appliances.dto.EmployeeUpdateDto;
import com.danil.appliances.model.Employee;
import com.danil.appliances.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/account/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeAccountController {

    private final EmployeeService employeeService;

    @GetMapping
    public String page(Authentication auth, Model model) {
        Employee employee= this.employeeService.getByEmail(auth.getName());

        if (!model.containsAttribute("profile")) {
            EmployeeUpdateDto dto = new EmployeeUpdateDto();
            dto.setName(employee.getName());
            dto.setDepartment(employee.getDepartment());
            model.addAttribute("profile", dto);
        }

        if (!model.containsAttribute("pwd")) {
            model.addAttribute("pwd", new ChangePasswordDto());
        }

        model.addAttribute("email", employee.getEmail());
        model.addAttribute("enabled", employee.isEnabled());
        return "account/employee/employeeAccount";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profile") EmployeeUpdateDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Authentication auth) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profile", bindingResult);
            redirectAttributes.addFlashAttribute("profile", dto);
            return "redirect:/account/employee";
        }
        this.employeeService.updateMyProfile(auth.getName(), dto);
        redirectAttributes.addFlashAttribute("success", "Profile updated");
        return "redirect:/account/employee";
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
            return "redirect:/account/employee";
        }

        this.employeeService.changeMyPassword(auth.getName(), dto.getCurrentPassword(), dto.getNewPassword());
        redirectAttributes.addFlashAttribute("success", "Password changed");
        return "redirect:/account/employee";
    }
}

