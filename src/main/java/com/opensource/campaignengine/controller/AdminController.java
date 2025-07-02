package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.repository.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/campaigns")
@PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
public class AdminController {

    private final CampaignRepository campaignRepository;

    public AdminController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        // Tüm kampanyaları veritabanından çek
        model.addAttribute("campaigns", campaignRepository.findAll());
        // "campaigns.html" isimli şablonu (view) göster
        return "campaigns";
    }

    @GetMapping("/new")
    public String showNewCampaignForm(Model model) {
        // Thymeleaf'in form ile nesneyi bağlayabilmesi için modele boş bir Campaign nesnesi ekliyoruz.
        model.addAttribute("campaign", new Campaign());
        // Kampanya tipi seçimi için Enum değerlerini de modele ekliyoruz.
        model.addAttribute("campaignTypes", CampaignType.values());
        return "campaign-form"; // campaign-form.html şablonunu göster
    }


    @PostMapping
    public String saveCampaign(@Valid @ModelAttribute("campaign") Campaign campaign, BindingResult bindingResult, Model model) {
        // Eğer validasyon hataları varsa
        if (bindingResult.hasErrors()) {

            model.addAttribute("campaignTypes", CampaignType.values());

            return "campaign-form";
        }

        campaignRepository.save(campaign);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/delete/{id}")
    public String deleteCampaign(@PathVariable("id") Long id) {
        campaignRepository.deleteById(id);
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/edit/{id}")
    public String showEditCampaignForm(@PathVariable("id") Long id, Model model) {
        // ID'si verilen kampanyayı veritabanında bul. Bulamazsan hata fırlat.
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz Kampanya Id:" + id));

        model.addAttribute("campaign", campaign);
        model.addAttribute("campaignTypes", CampaignType.values());
        return "campaign-form";
    }

}