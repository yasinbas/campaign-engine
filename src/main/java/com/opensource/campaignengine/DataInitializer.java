package com.opensource.campaignengine;

import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j // Lombok'un loglama için sunduğu kolaylık
public class DataInitializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;

    // Constructor Injection: Spring, bu bileşeni oluştururken ona bir CampaignRepository örneği verecek.
    public DataInitializer(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Veritabanı boş mu kontrol ediliyor...");

        // Eğer veritabanında zaten kampanya varsa, tekrar ekleme yapma.
        if (campaignRepository.count() == 0) {
            log.info("Veritabanı boş. Test verileri oluşturuluyor...");

            Campaign campaign1 = new Campaign();
            campaign1.setName("Gazozlarda 3 Al 2 Öde");
            campaign1.setDescription("Tüm gazlı içeceklerde geçerli efsane kampanya.");
            campaign1.setCampaignType(CampaignType.BUY_X_PAY_Y);
            campaign1.setDetails("{ \"productId\": \"GAZOZ-123\", \"buyQuantity\": 3, \"payQuantity\": 2 }");
            campaign1.setStartDate(LocalDateTime.now());
            campaign1.setEndDate(LocalDateTime.now().plusMonths(1));
            campaign1.setActive(true);
            campaign1.setPriority(10);

            Campaign campaign2 = new Campaign();
            campaign2.setName("500 TL Üzeri Sepete %10 İndirim");
            campaign2.setDescription("Tüm ürünlerde geçerli sepet indirimi.");
            campaign2.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
            campaign2.setDetails("{ \"minAmount\": 500.0, \"discountPercentage\": 10.0 }");
            campaign2.setStartDate(LocalDateTime.now());
            campaign2.setEndDate(LocalDateTime.now().plusDays(15));
            campaign2.setActive(true);
            campaign2.setPriority(5);

            campaignRepository.saveAll(List.of(campaign1, campaign2));

            log.info("{} adet test kampanyası veritabanına eklendi.", campaignRepository.count());
        } else {
            log.info("Veritabanında zaten veri var. Test verisi oluşturma atlandı.");
        }
    }
}