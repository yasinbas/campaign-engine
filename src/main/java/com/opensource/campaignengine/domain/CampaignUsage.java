package com.opensource.campaignengine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to track individual campaign applications and their impact.
 * This enables analytics on campaign performance and usage patterns.
 */
@Getter
@Setter
@Entity
@Table(name = "campaign_usage")
public class CampaignUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    /**
     * Original cart total before any discounts
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal originalCartTotal;

    /**
     * Discount amount applied by this campaign
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    /**
     * Final cart total after this campaign's discount
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal finalCartTotal;

    /**
     * Timestamp when the campaign was applied
     */
    @Column(nullable = false)
    private LocalDateTime appliedAt;

    /**
     * Optional identifier for the transaction/session where this campaign was applied
     */
    @Column(length = 100)
    private String transactionId;

    /**
     * Additional metadata about the campaign application (e.g., products affected)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    public CampaignUsage() {
        this.appliedAt = LocalDateTime.now();
    }

    public CampaignUsage(Campaign campaign, BigDecimal originalCartTotal, BigDecimal discountAmount, BigDecimal finalCartTotal) {
        this();
        this.campaign = campaign;
        this.originalCartTotal = originalCartTotal;
        this.discountAmount = discountAmount;
        this.finalCartTotal = finalCartTotal;
    }
}