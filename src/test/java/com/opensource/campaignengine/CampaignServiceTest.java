package com.opensource.campaignengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.dto.CartDTO;
import com.opensource.campaignengine.dto.CartItemDTO;
import com.opensource.campaignengine.dto.EvaluationResultDTO;
import com.opensource.campaignengine.repository.CampaignRepository;
import com.opensource.campaignengine.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


// JUnit 5'e Mockito özelliklerini kullanmasını söylüyoruz.
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    // Sahte (Mock) bir CampaignRepository oluştur. Bu, veritabanına gitmeyecek.
    @Mock
    private CampaignRepository campaignRepository;

    // Servisimiz ObjectMapper'a da bağımlı olduğu için onu da mock'luyoruz.
    @Mock
    private ObjectMapper objectMapper;

    // Test edeceğimiz asıl servis. Mockito, yukarıdaki sahte nesneleri bu servisin içine enjekte edecek.
    @InjectMocks
    private CampaignService campaignService;

    // Bu metodun bir test metodu olduğunu belirtiyoruz.
    @Test
    void shouldApplyBasketPercentageDiscount_WhenCartTotalExceedsMinimum() throws JsonProcessingException {
        // ARRANGE (Hazırlık Aşaması): Test için gerekli verileri ve sahte davranışları hazırla.

        // 1. Sahte bir kampanya oluştur.
        Campaign mockCampaign = new Campaign();
        mockCampaign.setId(1L);
        mockCampaign.setName("500 TL Üzeri Sepete %10 İndirim");
        mockCampaign.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
        mockCampaign.setDetails("{ \"minAmount\": 500.0, \"discountPercentage\": 10.0 }");

        // 2. Kampanyayı tetikleyecek bir alışveriş sepeti oluştur (Toplam > 500 TL).
        CartItemDTO item1 = new CartItemDTO();
        item1.setUnitPrice(new BigDecimal("300"));
        item1.setQuantity(2); // 2 * 300 = 600 TL
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(List.of(item1));

        // 3. Sahte nesnelerin davranışlarını tanımla.
        // "campaignRepository.findByActiveTrue() metodu çağrıldığında, bizim sahte kampanyamızı içeren bir liste döndür" diyoruz.
        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>(List.of(mockCampaign)));

        // "objectMapper.readTree() metodu çağrıldığında, sahte bir JSON nesnesi döndür" diyoruz.
        JsonNode mockJsonNode = new ObjectMapper().readTree(mockCampaign.getDetails());
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);


        // ACT (Eylem Aşaması): Test edilecek asıl metodu çağır.
        EvaluationResultDTO result = campaignService.evaluateCart(cartDTO);


        // ASSERT (Doğrulama Aşaması): Sonuçların beklediğimiz gibi olup olmadığını kontrol et.
        assertThat(result).isNotNull();
        // Orijinal toplam 600 olmalı.
        assertThat(result.getOriginalTotal()).isEqualByComparingTo("600");
        // İndirim (600 * %10 = 60) sonrası son tutar 540 olmalı.
        assertThat(result.getFinalTotal()).isEqualByComparingTo("540");
        // Sadece 1 adet indirim uygulanmış olmalı.
        assertThat(result.getAppliedDiscounts()).hasSize(1);
        // Uygulanan indirimin adı doğru mu?
        assertThat(result.getAppliedDiscounts().get(0).getCampaignName()).isEqualTo("500 TL Üzeri Sepete %10 İndirim");
    }

    // --- YENİ EKLENEN TEST METODU ---
    @Test
    void shouldApplyBuyXPayYDiscount_WhenProductQuantityIsSufficient() throws JsonProcessingException {
        // ARRANGE: Test verilerini ve sahte davranışları hazırla

        // 1. "3 Al 2 Öde" kampanyasını oluştur
        Campaign mockCampaign = new Campaign();
        mockCampaign.setId(2L);
        mockCampaign.setName("Gazozlarda 3 Al 2 Öde");
        mockCampaign.setCampaignType(CampaignType.BUY_X_PAY_Y);
        mockCampaign.setPriority(10); // Yüksek öncelikli
        mockCampaign.setDetails("{ \"productId\": \"GAZOZ-123\", \"buyQuantity\": 3, \"payQuantity\": 2 }");

        // 2. Bu kampanyayı tetikleyecek bir sepet oluştur (3'ten fazla gazoz içeriyor)
        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId("GAZOZ-123");
        item1.setQuantity(5); // 5 adet gazoz
        item1.setUnitPrice(new BigDecimal("20.0")); // Tanesi 20 TL
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(List.of(item1));

        // 3. Mock'ların davranışlarını tanımla
        // Repository çağrıldığında sahte kampanyamızı dönsün
        when(campaignRepository.findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>(List.of(mockCampaign)));

        // ObjectMapper çağrıldığında sahte JSON'umuzu dönsün
        JsonNode mockJsonNode = new ObjectMapper().readTree(mockCampaign.getDetails());
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);

        // ACT: Asıl metodu çağır
        EvaluationResultDTO result = campaignService.evaluateCart(cartDTO);


        // Orijinal toplam: 5 * 20 = 100 TL
        // Kampanya 3 al 2 öde olduğu için, 1 kez uygulanır ve 1 ürün bedava gelir. İndirim: 1 * 20 = 20 TL
        // Son tutar: 100 - 20 = 80 TL
        assertThat(result).isNotNull();
        assertThat(result.getOriginalTotal()).isEqualByComparingTo("100.0");
        assertThat(result.getFinalTotal()).isEqualByComparingTo("80.0");
        assertThat(result.getAppliedDiscounts()).hasSize(1);
        assertThat(result.getAppliedDiscounts().get(0).getDiscountAmount()).isEqualByComparingTo("20.0");
    }
}