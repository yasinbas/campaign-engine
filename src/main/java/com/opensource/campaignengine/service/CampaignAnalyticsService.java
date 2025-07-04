package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignUsage;
import com.opensource.campaignengine.dto.*;
import com.opensource.campaignengine.repository.CampaignRepository;
import com.opensource.campaignengine.repository.CampaignUsageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for campaign analytics and usage tracking
 */
@Slf4j
@Service
public class CampaignAnalyticsService {

    private final CampaignUsageRepository campaignUsageRepository;
    private final CampaignRepository campaignRepository;

    public CampaignAnalyticsService(CampaignUsageRepository campaignUsageRepository, 
                                  CampaignRepository campaignRepository) {
        this.campaignUsageRepository = campaignUsageRepository;
        this.campaignRepository = campaignRepository;
    }

    /**
     * Records the usage of a campaign when it's applied to a cart
     */
    @Transactional
    public void recordCampaignUsage(Campaign campaign, BigDecimal originalTotal, 
                                  BigDecimal discountAmount, BigDecimal finalTotal, 
                                  String transactionId) {
        CampaignUsage usage = new CampaignUsage(campaign, originalTotal, discountAmount, finalTotal);
        usage.setTransactionId(transactionId);
        
        campaignUsageRepository.save(usage);
        log.debug("Recorded campaign usage for campaign: {} with discount: {}", 
                 campaign.getName(), discountAmount);
    }

    /**
     * Get comprehensive analytics for all campaigns within a date range
     */
    public CampaignAnalyticsDTO getCampaignAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStatistics = campaignUsageRepository.getCampaignStatistics(startDate, endDate);
        
        List<CampaignStatisticsDTO> statistics = rawStatistics.stream()
            .map(this::mapToCampaignStatistics)
            .collect(Collectors.toList());

        // Add campaigns with zero usage
        List<Campaign> allCampaigns = campaignRepository.findAll();
        List<Long> usedCampaignIds = statistics.stream()
            .map(CampaignStatisticsDTO::getCampaignId)
            .collect(Collectors.toList());

        allCampaigns.stream()
            .filter(campaign -> !usedCampaignIds.contains(campaign.getId()))
            .forEach(campaign -> {
                CampaignStatisticsDTO zeroStats = new CampaignStatisticsDTO();
                zeroStats.setCampaignId(campaign.getId());
                zeroStats.setCampaignName(campaign.getName());
                zeroStats.setTotalApplications(0);
                zeroStats.setTotalDiscountAmount(BigDecimal.ZERO);
                zeroStats.setAverageDiscountAmount(BigDecimal.ZERO);
                zeroStats.setCampaignType(campaign.getCampaignType().toString());
                zeroStats.setActive(campaign.isActive());
                statistics.add(zeroStats);
            });

        BigDecimal totalDiscounts = statistics.stream()
            .map(CampaignStatisticsDTO::getTotalDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalApplications = statistics.stream()
            .mapToLong(CampaignStatisticsDTO::getTotalApplications)
            .sum();

        CampaignAnalyticsDTO analytics = new CampaignAnalyticsDTO();
        analytics.setCampaignStatistics(statistics);
        analytics.setTotalDiscountsProvided(totalDiscounts);
        analytics.setTotalCampaignApplications(totalApplications);
        analytics.setDateRangeStart(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        analytics.setDateRangeEnd(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return analytics;
    }

    /**
     * Get daily statistics for a specific campaign
     */
    public List<DailyStatisticsDTO> getDailyStatistics(Long campaignId, LocalDateTime startDate, LocalDateTime endDate) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + campaignId));

        List<Object[]> rawData = campaignUsageRepository.getDailyStatisticsByCampaign(campaign, startDate, endDate);
        
        return rawData.stream()
            .map(this::mapToDailyStatistics)
            .collect(Collectors.toList());
    }

    /**
     * Get statistics for a specific campaign
     */
    public CampaignStatisticsDTO getCampaignStatistics(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + campaignId));

        long totalApplications = campaignUsageRepository.countByCampaign(campaign);
        BigDecimal totalDiscountAmount = campaignUsageRepository.getTotalDiscountAmountByCampaign(campaign);
        BigDecimal averageDiscountAmount = totalApplications > 0 
            ? totalDiscountAmount.divide(BigDecimal.valueOf(totalApplications), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new CampaignStatisticsDTO(
            campaign.getId(),
            campaign.getName(),
            totalApplications,
            totalDiscountAmount,
            averageDiscountAmount,
            campaign.getCampaignType().toString(),
            campaign.isActive()
        );
    }

    private CampaignStatisticsDTO mapToCampaignStatistics(Object[] row) {
        Long campaignId = (Long) row[0];
        String campaignName = (String) row[1];
        Long count = (Long) row[2];
        BigDecimal totalDiscount = (BigDecimal) row[3];
        BigDecimal avgDiscount = (BigDecimal) row[4];

        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        String campaignType = campaign != null ? campaign.getCampaignType().toString() : "UNKNOWN";
        boolean isActive = campaign != null && campaign.isActive();

        return new CampaignStatisticsDTO(
            campaignId, campaignName, count, totalDiscount, avgDiscount, campaignType, isActive
        );
    }

    private DailyStatisticsDTO mapToDailyStatistics(Object[] row) {
        LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
        Long count = (Long) row[1];
        BigDecimal totalDiscount = (BigDecimal) row[2];

        return new DailyStatisticsDTO(date, count, totalDiscount);
    }
}