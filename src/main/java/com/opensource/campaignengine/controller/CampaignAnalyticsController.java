package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.dto.CampaignAnalyticsDTO;
import com.opensource.campaignengine.dto.CampaignStatisticsDTO;
import com.opensource.campaignengine.dto.DailyStatisticsDTO;
import com.opensource.campaignengine.service.CampaignAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for campaign analytics and reporting
 */
@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
public class CampaignAnalyticsController {

    private final CampaignAnalyticsService analyticsService;

    public CampaignAnalyticsController(CampaignAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Get campaign analytics for a date range",
            description = "Returns comprehensive analytics including total applications, discount amounts, and performance metrics for all campaigns within the specified date range.")
    @GetMapping("/campaigns")
    public CampaignAnalyticsDTO getCampaignAnalytics(
            @Parameter(description = "Start date for analytics (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "End date for analytics (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return analyticsService.getCampaignAnalytics(startDate, endDate);
    }

    @Operation(summary = "Get analytics for the last 30 days",
            description = "Returns campaign analytics for the last 30 days as a convenience endpoint.")
    @GetMapping("/campaigns/last30days")
    public CampaignAnalyticsDTO getLast30DaysAnalytics() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        return analyticsService.getCampaignAnalytics(startDate, endDate);
    }

    @Operation(summary = "Get statistics for a specific campaign",
            description = "Returns detailed statistics for a single campaign including total applications and discount amounts.")
    @GetMapping("/campaigns/{campaignId}")
    public CampaignStatisticsDTO getCampaignStatistics(
            @Parameter(description = "Campaign ID") @PathVariable Long campaignId) {
        return analyticsService.getCampaignStatistics(campaignId);
    }

    @Operation(summary = "Get daily statistics for a specific campaign",
            description = "Returns day-by-day usage statistics for a campaign within the specified date range.")
    @GetMapping("/campaigns/{campaignId}/daily")
    public List<DailyStatisticsDTO> getDailyStatistics(
            @Parameter(description = "Campaign ID") @PathVariable Long campaignId,
            
            @Parameter(description = "Start date for daily statistics") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "End date for daily statistics")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return analyticsService.getDailyStatistics(campaignId, startDate, endDate);
    }
}