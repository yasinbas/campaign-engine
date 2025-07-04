package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.domain.CampaignUsage;
import com.opensource.campaignengine.dto.CampaignAnalyticsDTO;
import com.opensource.campaignengine.dto.CampaignStatisticsDTO;
import com.opensource.campaignengine.dto.DailyStatisticsDTO;
import com.opensource.campaignengine.repository.CampaignRepository;
import com.opensource.campaignengine.repository.CampaignUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignAnalyticsServiceTest {

    @Mock
    private CampaignUsageRepository campaignUsageRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignAnalyticsService analyticsService;

    private Campaign testCampaign;
    private CampaignUsage testUsage;

    @BeforeEach
    void setUp() {
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Test Campaign");
        testCampaign.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
        testCampaign.setActive(true);

        testUsage = new CampaignUsage();
        testUsage.setId(1L);
        testUsage.setCampaign(testCampaign);
        testUsage.setOriginalCartTotal(new BigDecimal("100.00"));
        testUsage.setDiscountAmount(new BigDecimal("10.00"));
        testUsage.setFinalCartTotal(new BigDecimal("90.00"));
        testUsage.setAppliedAt(LocalDateTime.now());
    }

    @Test
    void shouldRecordCampaignUsage() {
        // ARRANGE
        when(campaignUsageRepository.save(any(CampaignUsage.class))).thenReturn(testUsage);

        // ACT
        analyticsService.recordCampaignUsage(
            testCampaign,
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            new BigDecimal("90.00"),
            "TXN-123"
        );

        // ASSERT
        verify(campaignUsageRepository).save(any(CampaignUsage.class));
    }

    @Test
    void shouldGetCampaignStatistics() {
        // ARRANGE
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignUsageRepository.countByCampaign(testCampaign)).thenReturn(5L);
        when(campaignUsageRepository.getTotalDiscountAmountByCampaign(testCampaign))
            .thenReturn(new BigDecimal("50.00"));

        // ACT
        CampaignStatisticsDTO result = analyticsService.getCampaignStatistics(1L);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getCampaignId()).isEqualTo(1L);
        assertThat(result.getCampaignName()).isEqualTo("Test Campaign");
        assertThat(result.getTotalApplications()).isEqualTo(5L);
        assertThat(result.getTotalDiscountAmount()).isEqualByComparingTo("50.00");
        assertThat(result.getAverageDiscountAmount()).isEqualByComparingTo("10.00");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void shouldGetCampaignAnalytics() {
        // ARRANGE
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> mockStatistics = new ArrayList<>();
        mockStatistics.add(new Object[]{1L, "Test Campaign", 5L, new BigDecimal("50.00"), new BigDecimal("10.00")});

        when(campaignUsageRepository.getCampaignStatistics(startDate, endDate))
            .thenReturn(mockStatistics);
        when(campaignRepository.findAll()).thenReturn(new ArrayList<>(List.of(testCampaign)));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        // ACT
        CampaignAnalyticsDTO result = analyticsService.getCampaignAnalytics(startDate, endDate);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getCampaignStatistics()).hasSize(1);
        assertThat(result.getTotalDiscountsProvided()).isEqualByComparingTo("50.00");
        assertThat(result.getTotalCampaignApplications()).isEqualTo(5L);
    }

    @Test
    void shouldGetDailyStatistics() {
        // ARRANGE
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        
        List<Object[]> mockDailyData = new ArrayList<>();
        mockDailyData.add(new Object[]{java.sql.Date.valueOf("2024-01-01"), 3L, new BigDecimal("30.00")});
        mockDailyData.add(new Object[]{java.sql.Date.valueOf("2024-01-02"), 2L, new BigDecimal("20.00")});
        
        when(campaignUsageRepository.getDailyStatisticsByCampaign(testCampaign, startDate, endDate))
            .thenReturn(mockDailyData);

        // ACT
        List<DailyStatisticsDTO> result = analyticsService.getDailyStatistics(1L, startDate, endDate);

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getApplicationCount()).isEqualTo(3L);
        assertThat(result.get(0).getTotalDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.get(1).getApplicationCount()).isEqualTo(2L);
        assertThat(result.get(1).getTotalDiscountAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldThrowExceptionForInvalidCampaignId() {
        // ARRANGE
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> analyticsService.getCampaignStatistics(999L)
        );
    }
}