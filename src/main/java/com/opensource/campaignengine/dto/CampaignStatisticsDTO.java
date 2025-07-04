package com.opensource.campaignengine.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO for campaign performance statistics
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignStatisticsDTO {
    private Long campaignId;
    private String campaignName;
    private long totalApplications;
    private BigDecimal totalDiscountAmount;
    private BigDecimal averageDiscountAmount;
    private String campaignType;
    private boolean isActive;
}