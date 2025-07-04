package com.opensource.campaignengine.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for comprehensive analytics response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignAnalyticsDTO {
    private List<CampaignStatisticsDTO> campaignStatistics;
    private BigDecimal totalDiscountsProvided;
    private long totalCampaignApplications;
    private String dateRangeStart;
    private String dateRangeEnd;
}