package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignUsageRepository extends JpaRepository<CampaignUsage, Long> {

    /**
     * Find all usage records for a specific campaign
     */
    List<CampaignUsage> findByCampaignOrderByAppliedAtDesc(Campaign campaign);

    /**
     * Find usage records within a date range
     */
    List<CampaignUsage> findByAppliedAtBetweenOrderByAppliedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count total applications for a campaign
     */
    long countByCampaign(Campaign campaign);

    /**
     * Get total discount amount provided by a campaign
     */
    @Query("SELECT COALESCE(SUM(cu.discountAmount), 0) FROM CampaignUsage cu WHERE cu.campaign = :campaign")
    BigDecimal getTotalDiscountAmountByCampaign(@Param("campaign") Campaign campaign);

    /**
     * Get campaign usage statistics within a date range
     */
    @Query("SELECT cu.campaign.id, cu.campaign.name, COUNT(cu), SUM(cu.discountAmount), AVG(cu.discountAmount) " +
           "FROM CampaignUsage cu " +
           "WHERE cu.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY cu.campaign.id, cu.campaign.name " +
           "ORDER BY SUM(cu.discountAmount) DESC")
    List<Object[]> getCampaignStatistics(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Get daily usage statistics for a campaign
     */
    @Query("SELECT DATE(cu.appliedAt), COUNT(cu), SUM(cu.discountAmount) " +
           "FROM CampaignUsage cu " +
           "WHERE cu.campaign = :campaign AND cu.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(cu.appliedAt) " +
           "ORDER BY DATE(cu.appliedAt)")
    List<Object[]> getDailyStatisticsByCampaign(@Param("campaign") Campaign campaign,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}