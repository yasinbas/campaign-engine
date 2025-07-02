package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.Product;
import com.opensource.campaignengine.domain.ProductSaleType;
import com.opensource.campaignengine.repository.CategoryRepository;
import com.opensource.campaignengine.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')") // Bu sayfaya sadece ADMIN rolü olanların erişebilmesini sağlıyoruz
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository; // Kategori listesi için eklendi

    public ProductAdminController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products";
    }

    // YENİ: Yeni ürün ekleme formunu gösterir
    @GetMapping("/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        // Formdaki dropdown'lar için gerekli verileri modele ekliyoruz
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("saleTypes", ProductSaleType.values());
        return "admin/product-form";
    }

    // YENİ: Mevcut ürünü düzenleme formunu gösterir
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz Ürün Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("saleTypes", ProductSaleType.values());
        return "admin/product-form";
    }

    // YENİ: Hem yeni ürün eklemeyi hem de güncellemeyi yönetir
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productService.save(product);
        return "redirect:/admin/products";
    }

    // YENİ: Bir ürünü siler
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }
}