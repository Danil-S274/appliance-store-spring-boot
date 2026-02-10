package com.danil.appliances.controller;

import com.danil.appliances.dto.appliance.ApplianceDto;
import com.danil.appliances.dto.appliance.ApplianceSearchFilter;
import com.danil.appliances.mapper.ApplianceMapper;
import com.danil.appliances.mapper.ManufacturerMapper;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Category;
import com.danil.appliances.model.PowerType;
import com.danil.appliances.service.ApplianceService;
import com.danil.appliances.service.ManufacturerService;
import com.danil.appliances.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class CatalogController {

    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;
    private final OrderCommandService orderCommandService;

    private final ApplianceMapper applianceMapper;
    private final ManufacturerMapper manufacturerMapper;

    @GetMapping
    public String list(@ModelAttribute("filter") ApplianceSearchFilter filter,
                       @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Appliance> page = this.applianceService.search(filter, pageable);
        Page<ApplianceDto> pageDto = page.map(this.applianceMapper::toDto);

        model.addAttribute("page", pageDto);
        model.addAttribute("appliances", pageDto.getContent());

        model.addAttribute("categories", Category.values());
        model.addAttribute("powerTypes", PowerType.values());

        model.addAttribute("manufacturers",
                this.manufacturerService.findAll().stream()
                        .map(this.manufacturerMapper::toDto)
                        .toList());

        model.addAttribute("sortOptions", new String[]{
                "id,desc", "id,asc",
                "price,asc", "price,desc",
                "name,asc", "name,desc",
                "power,asc", "power,desc"
        });

        return "catalog/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Appliance appliance = this.applianceService.findById(id);
        model.addAttribute("appliance", this.applianceMapper.toDto(appliance));
        return "catalog/details";
    }

    @PostMapping("/{id}/add-to-cart")
    @PreAuthorize("hasRole('CLIENT')")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") long quantity,
                            Authentication auth) {
        this.orderCommandService.addItemToDraft(auth.getName(), id, quantity);
        return "redirect:/cart";
    }
}
