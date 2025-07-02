package com.opensource.campaignengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.dto.AppliedDiscountDTO;
import com.opensource.campaignengine.dto.CartDTO;
import com.opensource.campaignengine.dto.EvaluationResultDTO;
import com.opensource.campaignengine.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    public CampaignService(CampaignRepository campaignRepository, ObjectMapper objectMapper) {
        this.campaignRepository = campaignRepository;
        this.objectMapper = objectMapper;
    }

    public List<Campaign> findAllActiveCampaigns() {
        return campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime.now(), LocalDateTime.now());
    }

    public EvaluationResultDTO evaluateCart(CartDTO cart) {
        List<Campaign> applicableCampaigns = this.findAllActiveCampaigns();
        applicableCampaigns.sort(Comparator.comparingInt(Campaign::getPriority).reversed());

        // 1. Orijinal Toplamı BigDecimal uyumlu şekilde hesapla
        BigDecimal originalTotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(item.getQuantity())) // DÜZELTİLDİ
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AppliedDiscountDTO> appliedDiscounts = new ArrayList<>();
        Set<String> discountedProductIds = new HashSet<>();

        for (Campaign campaign : applicableCampaigns) {
            try {
                JsonNode details = objectMapper.readTree(campaign.getDetails());

                // --- KAMPANYA TİPİ: X AL Y ÖDE (BigDecimal için yeniden yazıldı) ---
                if (campaign.getCampaignType() == CampaignType.BUY_X_PAY_Y) {
                    String targetProductId = details.get("productId").asText();

                    if (!discountedProductIds.contains(targetProductId)) {
                        BigDecimal buyQuantity = new BigDecimal(details.get("buyQuantity").asText());
                        BigDecimal payQuantity = new BigDecimal(details.get("payQuantity").asText());

                        cart.getItems().stream()
                                .filter(item -> item.getProductId().equals(targetProductId) && item.getQuantity().compareTo(buyQuantity) >= 0) // DÜZELTİLDİ: >= yerine .compareTo()
                                .findFirst()
                                .ifPresent(item -> {
                                    BigDecimal timesApplicable = item.getQuantity().divide(buyQuantity, 0, RoundingMode.FLOOR); // DÜZELTİLDİ: / yerine .divide()
                                    if (timesApplicable.compareTo(BigDecimal.ZERO) > 0) {
                                        BigDecimal freeAmount = buyQuantity.subtract(payQuantity); // DÜZELTİLDİ: - yerine .subtract()
                                        BigDecimal discountAmount = item.getUnitPrice().multiply(freeAmount).multiply(timesApplicable); // DÜZELTİLDİ: * yerine .multiply()

                                        appliedDiscounts.add(new AppliedDiscountDTO(campaign.getName(), discountAmount));
                                        discountedProductIds.add(targetProductId);
                                    }
                                });
                    }
                }
                // --- KAMPANYA TİPİ: SEPET TOPLAMINA YÜZDE İNDİRİMİ ---
                else if (campaign.getCampaignType() == CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT) {
                    BigDecimal minAmount = new BigDecimal(details.get("minAmount").asText());
                    if (originalTotal.compareTo(minAmount) >= 0) {
                        BigDecimal discountPercentage = new BigDecimal(details.get("discountPercentage").asText());
                        BigDecimal discountAmount = originalTotal.multiply(discountPercentage.divide(new BigDecimal("100")));

                        appliedDiscounts.add(new AppliedDiscountDTO(campaign.getName(), discountAmount));
                    }
                }
                // ... Diğer kampanya tipleri buraya eklenecek ...

            } catch (JsonProcessingException e) {
                log.error("Kampanya JSON parse hatası: Campaign ID = {}", campaign.getId(), e);
            }
        }

        BigDecimal totalDiscount = appliedDiscounts.stream()
                .map(AppliedDiscountDTO::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = originalTotal.subtract(totalDiscount);

        EvaluationResultDTO result = new EvaluationResultDTO();
        result.setOriginalTotal(originalTotal);
        result.setFinalTotal(finalTotal);
        result.setAppliedDiscounts(appliedDiscounts);
        return result;
    }
}