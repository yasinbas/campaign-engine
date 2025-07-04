package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.dto.CampaignAnalyticsDTO;
import com.opensource.campaignengine.service.CampaignAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web controller for campaign analytics admin panel
 */
@Controller
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
public class AnalyticsAdminController {

    private final CampaignAnalyticsService analyticsService;

    public AnalyticsAdminController(CampaignAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public String showAnalyticsDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        LocalDateTime start;
        LocalDateTime end;

        // Default to last 30 days if no dates provided
        if (startDate == null || endDate == null) {
            end = LocalDateTime.now();
            start = end.minusDays(30);
        } else {
            try {
                start = LocalDateTime.parse(startDate + "T00:00:00");
                end = LocalDateTime.parse(endDate + "T23:59:59");
            } catch (Exception e) {
                // Fall back to last 30 days if parsing fails
                end = LocalDateTime.now();
                start = end.minusDays(30);
            }
        }

        CampaignAnalyticsDTO analytics = analyticsService.getCampaignAnalytics(start, end);

        model.addAttribute("analytics", analytics);
        model.addAttribute("startDate", start.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("endDate", end.format(DateTimeFormatter.ISO_LOCAL_DATE));

        return "analytics-dashboard";
    }
}