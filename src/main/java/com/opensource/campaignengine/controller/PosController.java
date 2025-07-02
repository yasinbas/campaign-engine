package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.Product;
import com.opensource.campaignengine.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pos")
// Bu sayfaya giriş yapmış herhangi bir kullanıcının erişebilmesini sağlıyoruz
@PreAuthorize("isAuthenticated()")
public class PosController {

    private final ProductService productService;

    public PosController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String showPosPage(Model model) {
        // Sadece aktif olan ürünleri POS ekranında gösterelim
        model.addAttribute("products", productService.findAll().stream().filter(Product::isActive).toList());
        return "pos/pos-screen"; // templates/pos/pos-screen.html dosyasını arayacak
    }
}