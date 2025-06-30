package com.opensource.campaignengine.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class EvaluationResultDTO {
    private BigDecimal originalTotal;
    private BigDecimal finalTotal;
    private List<AppliedDiscountDTO> appliedDiscounts;
}