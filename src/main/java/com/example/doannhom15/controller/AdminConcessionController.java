package com.example.doannhom15.controller;

import com.example.doannhom15.model.ConcessionItem;
import com.example.doannhom15.service.ConcessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/concessions")
@RequiredArgsConstructor
public class AdminConcessionController {

    private final ConcessionService concessionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", concessionService.findAll());
        return "admin/concessions";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("item", ConcessionItem.builder()
                .price(BigDecimal.ZERO)
                .type(ConcessionItem.ConcessionType.BAP)
                .build());
        return "admin/concession-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ConcessionItem item, RedirectAttributes ra) {
        concessionService.save(item);
        ra.addFlashAttribute("success", item.getId() == null ? "Thêm đồ ăn/uống thành công!" : "Cập nhật thành công!");
        return "redirect:/admin/concessions";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        ConcessionItem item = concessionService.getById(id);
        if (item == null) return "redirect:/admin/concessions";
        model.addAttribute("item", item);
        return "admin/concession-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        concessionService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa!");
        return "redirect:/admin/concessions";
    }
}
