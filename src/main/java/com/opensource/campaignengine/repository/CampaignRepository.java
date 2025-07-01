package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

        /**
         * Belirtilen bir zaman diliminde aktif olan tüm kampanyaları bulur.
         * Bu metot, Spring Data JPA tarafından otomatik olarak şu anlama gelen bir SQL sorgusuna çevrilir:
         * "SELECT * FROM campaigns WHERE active = true AND start_date < [şimdi] AND end_date > [şimdi]"
         *
         * @param now Mevcut zaman
         * @param nowAgain Mevcut zaman (ikinci tarih parametresi için)
         * @return Geçerli ve aktif kampanyaların listesi
         */
        List<Campaign> findAllByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime now, LocalDateTime nowAgain);

}