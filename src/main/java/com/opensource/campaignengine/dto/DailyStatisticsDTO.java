package com.opensource.campaignengine.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for daily campaign usage statistics
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatisticsDTO {
    private LocalDate date;
    private long applicationCount;
    private BigDecimal totalDiscountAmount;
}