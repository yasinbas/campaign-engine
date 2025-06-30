package com.opensource.campaignengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor // Tüm alanları alan bir constructor oluşturur
public class AppliedDiscountDTO {
    private String campaignName;
    private BigDecimal discountAmount;
}