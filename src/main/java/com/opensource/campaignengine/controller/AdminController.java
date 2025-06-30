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

@Controller
@RequestMapping("/admin/campaigns")
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
    public String saveCampaign(@ModelAttribute("campaign") Campaign campaign) {
        campaignRepository.save(campaign);
        return "redirect:/admin/campaigns"; // Kaydettikten sonra kampanya listesi sayfasına yönlendir
    }

}