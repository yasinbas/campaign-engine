package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.dto.CartDTO; // EKLENDİ
import com.opensource.campaignengine.dto.EvaluationResultDTO; // EKLENDİ
import com.opensource.campaignengine.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*; // EKLENDİ

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "Tüm Aktif Kampanyaları Listeler",
            description = "Veritabanında 'active' bayrağı true olan tüm kampanyaları getirir.")
    @GetMapping("/active")
    public List<Campaign> getActiveCampaigns() {
        return campaignService.findAllActiveCampaigns();
    }

    // YENİ EKLENEN ENDPOINT
    @PostMapping("/evaluate")
    public EvaluationResultDTO evaluateCart(@RequestBody CartDTO cart) {
        return campaignService.evaluateCart(cart);
    }
}