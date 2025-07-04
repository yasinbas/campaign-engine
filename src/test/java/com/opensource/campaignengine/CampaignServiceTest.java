package com.opensource.campaignengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.dto.CartDTO;
import com.opensource.campaignengine.dto.CartItemDTO;
import com.opensource.campaignengine.dto.EvaluationResultDTO;
import com.opensource.campaignengine.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    
    @Mock
    private CampaignAnalyticsService analyticsService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CampaignService campaignService;

    // Her testten önce sahte kampanyaları döndürecek olan repository'yi hazırlayalım.
    // Bu, kod tekrarını azaltır.
    @BeforeEach
    void setUp() {
        // Varsayılan olarak, repository'nin boş bir liste döndüğünü varsayalım.
        // Her test kendi ihtiyacına göre bu davranışı ezecektir (override).
        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any()))
                .thenReturn(new ArrayList<>());
    }

    @Test
    void shouldApplyBasketPercentageDiscount_WhenCartTotalExceedsMinimum() throws JsonProcessingException {
        // ARRANGE
        Campaign mockCampaign = new Campaign();
        mockCampaign.setName("500 TL Üzeri Sepete %10 İndirim");
        mockCampaign.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
        mockCampaign.setDetails("{ \"minAmount\": 500.0, \"discountPercentage\": 10.0 }");

        CartItemDTO item1 = new CartItemDTO();
        item1.setUnitPrice(new BigDecimal("300"));
        item1.setQuantity(new BigDecimal("2")); // DÜZELTİLDİ

        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(List.of(item1));

        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any()))
                .thenReturn(new ArrayList<>(List.of(mockCampaign)));

        JsonNode mockJsonNode = new ObjectMapper().readTree(mockCampaign.getDetails());
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);

        // ACT
        EvaluationResultDTO result = campaignService.evaluateCart(cartDTO);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOriginalTotal()).isEqualByComparingTo("600");
        assertThat(result.getFinalTotal()).isEqualByComparingTo("540"); // 600 - 60 (indirim)
    }

    @Test
    void shouldApplyBuyXPayYDiscount_WhenProductQuantityIsSufficient() throws JsonProcessingException {
        // ARRANGE
        Campaign mockCampaign = new Campaign();
        mockCampaign.setName("Gazozlarda 3 Al 2 Öde");
        mockCampaign.setCampaignType(CampaignType.BUY_X_PAY_Y);
        mockCampaign.setDetails("{ \"productId\": \"GAZOZ-123\", \"buyQuantity\": 3, \"payQuantity\": 2 }");

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId("GAZOZ-123");
        item1.setQuantity(new BigDecimal("5")); // DÜZELTİLDİ
        item1.setUnitPrice(new BigDecimal("20.0"));

        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(List.of(item1));

        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any()))
                .thenReturn(new ArrayList<>(List.of(mockCampaign)));

        JsonNode mockJsonNode = new ObjectMapper().readTree(mockCampaign.getDetails());
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);

        // ACT
        EvaluationResultDTO result = campaignService.evaluateCart(cartDTO);

        // ASSERT
        assertThat(result.getFinalTotal()).isEqualByComparingTo("80.0"); // 100 - 20 (1 adet bedava)
        assertThat(result.getAppliedDiscounts()).hasSize(1);
        assertThat(result.getAppliedDiscounts().get(0).getDiscountAmount()).isEqualByComparingTo("20.0");
    }

    // --- YENİ EKLENEN TEST ---
    @Test
    void shouldApplyBuyXPayYDiscount_ForWeightedProduct() throws JsonProcessingException {
        // ARRANGE
        Campaign mockCampaign = new Campaign();
        mockCampaign.setName("Elmada 1.5 KG al 1 KG öde");
        mockCampaign.setCampaignType(CampaignType.BUY_X_PAY_Y);
        mockCampaign.setDetails("{ \"productId\": \"ELMA-456\", \"buyQuantity\": 1.5, \"payQuantity\": 1.0 }");

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId("ELMA-456");
        item1.setQuantity(new BigDecimal("4.0")); // 4 kg elma
        item1.setUnitPrice(new BigDecimal("50.0")); // KG fiyatı 50 TL

        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(List.of(item1));

        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(), any()))
                .thenReturn(new ArrayList<>(List.of(mockCampaign)));

        JsonNode mockJsonNode = new ObjectMapper().readTree(mockCampaign.getDetails());
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);

        // ACT
        EvaluationResultDTO result = campaignService.evaluateCart(cartDTO);

        // ASSERT
        // Orijinal Tutar: 4 kg * 50 TL/kg = 200 TL
        // Kampanya 1.5 kg'da 0.5 kg indirim veriyor. 4 kg içinde 2 kez uygulanır (4 / 1.5 = 2).
        // Toplam indirimli miktar: 2 * 0.5 kg = 1 kg.
        // İndirim Tutarı: 1 kg * 50 TL/kg = 50 TL.
        // Son Tutar: 200 - 50 = 150 TL.
        assertThat(result.getFinalTotal()).isEqualByComparingTo("150.0");
        assertThat(result.getAppliedDiscounts()).hasSize(1);
        assertThat(result.getAppliedDiscounts().get(0).getDiscountAmount()).isEqualByComparingTo("50.0");
    }
}