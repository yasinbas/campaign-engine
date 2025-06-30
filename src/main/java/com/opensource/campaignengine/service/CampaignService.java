package com.opensource.campaignengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.dto.AppliedDiscountDTO;
import com.opensource.campaignengine.dto.CartDTO;
import com.opensource.campaignengine.dto.EvaluationResultDTO;
import com.opensource.campaignengine.repository.CampaignRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper; // JSON işlemek için

    public CampaignService(CampaignRepository campaignRepository, ObjectMapper objectMapper) {
        this.campaignRepository = campaignRepository;
        this.objectMapper = objectMapper;
    }

    public List<Campaign> findAllActiveCampaigns() {
        return campaignRepository.findByActiveTrue();
    }

    // YENİ EKLENEN METOT
    public EvaluationResultDTO evaluateCart(CartDTO cart) {
        // 1. Aktif kampanyaları ve sepetin orijinal toplamını al
        List<Campaign> activeCampaigns = campaignRepository.findByActiveTrue();
        BigDecimal originalTotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = new BigDecimal(originalTotal.toString());
        List<AppliedDiscountDTO> appliedDiscounts = new ArrayList<>();

        // 2. Her kampanyayı sırayla sepete uygula
        for (Campaign campaign : activeCampaigns) {
            try {
                // Not: Daha gelişmiş bir sistemde kampanyaların çakışmaması için öncelik sırasına göre
                // ve bir ürün/sepete sadece bir kampanyanın uygulanması gibi kurallar eklenebilir.
                // Şimdilik basit tutuyoruz.

                if (campaign.getCampaignType() == com.opensource.campaignengine.domain.CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT) {
                    JsonNode details = objectMapper.readTree(campaign.getDetails());
                    BigDecimal minAmount = new BigDecimal(details.get("minAmount").asText());

                    if (originalTotal.compareTo(minAmount) >= 0) {
                        BigDecimal discountPercentage = new BigDecimal(details.get("discountPercentage").asText());
                        BigDecimal discountAmount = originalTotal.multiply(discountPercentage.divide(new BigDecimal("100")));
                        finalTotal = finalTotal.subtract(discountAmount);
                        appliedDiscounts.add(new AppliedDiscountDTO(campaign.getName(), discountAmount));
                    }
                }

                // Diğer kampanya tipleri için de benzer "if" blokları eklenecek...
                // Örnek olarak şimdilik sadece sepet indirimi eklendi.

            } catch (JsonProcessingException e) {
                // Hatalı JSON formatı varsa bu kampanyayı atla
                System.err.println("Kampanya JSON parse hatası: " + campaign.getId());
            }
        }

        // 3. Sonucu oluştur ve geri dön
        EvaluationResultDTO result = new EvaluationResultDTO();
        result.setOriginalTotal(originalTotal);
        result.setFinalTotal(finalTotal);
        result.setAppliedDiscounts(appliedDiscounts);

        return result;
    }
}