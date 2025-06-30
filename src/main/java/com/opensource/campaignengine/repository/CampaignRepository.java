package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Campaign entity'si için veritabanı işlemlerini (CRUD - Create, Read, Update, Delete)
 * yöneten Spring Data JPA repository arayüzü.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

        List<Campaign> findByActiveTrue();
}

