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
        // Artık sadece o an geçerli olan kampanyaları getiriyoruz.
        return campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime.now(), LocalDateTime.now());
    }

    public EvaluationResultDTO evaluateCart(CartDTO cart) {
        // 1. Tarihe göre geçerli ve aktif kampanyaları al
        List<Campaign> applicableCampaigns = this.findAllActiveCampaigns();

        // 2. Kampanyaları öncelik sırasına göre (yüksek olan önce) sırala
        applicableCampaigns.sort(Comparator.comparingInt(Campaign::getPriority).reversed());

        // 3. Sepet toplamlarını ve indirim listesini hazırla
        BigDecimal originalTotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = new BigDecimal(originalTotal.toString());
        List<AppliedDiscountDTO> appliedDiscounts = new ArrayList<>();

        // 4. Hangi ürünlerin zaten indirim aldığını takip etmek için bir set oluştur
        Set<String> discountedProductIds = new HashSet<>();

        // 5. Her kampanyayı sıralı bir şekilde sepete uygula
        for (Campaign campaign : applicableCampaigns) {
            try {
                JsonNode details = objectMapper.readTree(campaign.getDetails());

                // --- KAMPANYA TİPİ: X AL Y ÖDE ---
                if (campaign.getCampaignType() == CampaignType.BUY_X_PAY_Y) {
                    String targetProductId = details.get("productId").asText();

                    // Eğer bu ürün daha önce başka bir kampanyadan indirim almadıysa devam et
                    if (!discountedProductIds.contains(targetProductId)) {
                        int buyQuantity = details.get("buyQuantity").asInt();
                        int payQuantity = details.get("payQuantity").asInt();

                        // Sepetteki ilgili ürünü bul ve koşulları kontrol et
                        cart.getItems().stream()
                                .filter(item -> item.getProductId().equals(targetProductId) && item.getQuantity() >= buyQuantity)
                                .findFirst()
                                .ifPresent(item -> {
                                    int timesApplicable = item.getQuantity() / buyQuantity;
                                    if (timesApplicable > 0) {
                                        int freeItemsCount = (buyQuantity - payQuantity) * timesApplicable;
                                        BigDecimal discountAmount = item.getUnitPrice().multiply(new BigDecimal(freeItemsCount));

                                        appliedDiscounts.add(new AppliedDiscountDTO(campaign.getName(), discountAmount));

                                        // Bu ürünü "indirim uygulandı" olarak işaretle ki başka kampanyadan etkilenmesin
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

            } catch (JsonProcessingException e) {
                log.error("Kampanya JSON parse hatası: Campaign ID = {}", campaign.getId(), e);
            }
        }

        // 6. Toplam indirim miktarını hesapla ve son tutarı bul
        BigDecimal totalDiscount = appliedDiscounts.stream()
                .map(AppliedDiscountDTO::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        finalTotal = originalTotal.subtract(totalDiscount);

        // 7. Sonucu oluştur ve geri dön
        EvaluationResultDTO result = new EvaluationResultDTO();
        result.setOriginalTotal(originalTotal);
        result.setFinalTotal(finalTotal);
        result.setAppliedDiscounts(appliedDiscounts);
        return result;
    }
}