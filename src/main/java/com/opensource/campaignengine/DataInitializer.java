package com.opensource.campaignengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.Campaign;
import com.opensource.campaignengine.domain.CampaignType;
import com.opensource.campaignengine.domain.Role;
import com.opensource.campaignengine.domain.User;
import com.opensource.campaignengine.repository.CampaignRepository;
import com.opensource.campaignengine.repository.RoleRepository;
import com.opensource.campaignengine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper; // JSON oluşturmak için eklendi

    public DataInitializer(CampaignRepository campaignRepository, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper; // Constructor'a eklendi
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRolesAndUsers();
        createTestCampaigns();
    }


    protected void createRolesAndUsers() {
        // Bu metot zaten mükemmel ve idempotent, dokunmuyoruz.
        log.info("Kullanıcı ve Rol kontrolü yapılıyor...");
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            log.info("ROLE_ADMIN oluşturuldu.");
        }
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
            log.info("ROLE_USER oluşturuldu.");
        }
        if (userRepository.count() == 0) {
            log.info("Hiç kullanıcı bulunamadı. Varsayılan 'admin' kullanıcısı oluşturuluyor...");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setEnabled(true);
            adminUser.setRoles(Set.of(adminRole));
            userRepository.save(adminUser);
            log.info("Varsayılan 'admin' kullanıcısı (şifre: password) oluşturuldu.");
        } else {
            log.info("Veritabanında zaten kullanıcı mevcut. Kullanıcı oluşturma atlandı.");
        }
    }

    private void createTestCampaigns() {
        log.info("Kampanya kontrolü yapılıyor...");
        if (campaignRepository.count() == 0) {
            log.info("Veritabanı boş. Test kampanyaları oluşturuluyor...");
            try {
                // Kampanya 1: BUY_X_PAY_Y
                Campaign campaign1 = new Campaign();
                campaign1.setName("Gazozlarda 3 Al 2 Öde");
                campaign1.setCampaignType(CampaignType.BUY_X_PAY_Y);
                Map<String, Object> details1 = Map.of(
                        "productId", "GAZOZ-123",
                        "buyQuantity", 3,
                        "payQuantity", 2
                );
                campaign1.setDetails(objectMapper.writeValueAsString(details1));
                campaign1.setStartDate(LocalDateTime.now().minusDays(1));
                campaign1.setEndDate(LocalDateTime.now().plusMonths(1));
                campaign1.setActive(true);
                campaign1.setPriority(10);

                // Kampanya 2: BASKET_TOTAL_PERCENTAGE_DISCOUNT
                Campaign campaign2 = new Campaign();
                campaign2.setName("500 TL Üzeri Sepete %10 İndirim");
                campaign2.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
                Map<String, Object> details2 = Map.of(
                        "minAmount", 500.0,
                        "discountPercentage", 10.0
                );
                campaign2.setDetails(objectMapper.writeValueAsString(details2));
                campaign2.setStartDate(LocalDateTime.now().minusDays(1));
                campaign2.setEndDate(LocalDateTime.now().plusDays(15));
                campaign2.setActive(true);
                campaign2.setPriority(5);

                // Kampanya 3: BUY_A_GET_B_DISCOUNT (Yarım kalan özelliğimiz için test verisi)
                Campaign campaign3 = new Campaign();
                campaign3.setName("Laptop Alana Mouse %50 İndirimli");
                campaign3.setCampaignType(CampaignType.BUY_A_GET_B_DISCOUNT);
                Map<String, Object> details3 = Map.of(
                        "triggerProductIds", List.of("LAPTOP-001"),
                        "rewardProductIds", List.of("MOUSE-002"),
                        "rewardType", "PERCENTAGE",
                        "rewardValue", 50
                );
                campaign3.setDetails(objectMapper.writeValueAsString(details3));
                campaign3.setStartDate(LocalDateTime.now().minusDays(1));
                campaign3.setEndDate(LocalDateTime.now().plusMonths(2));
                campaign3.setActive(true);
                campaign3.setPriority(20);

                campaignRepository.saveAll(List.of(campaign1, campaign2, campaign3));

                log.info("{} adet test kampanyası veritabanına eklendi.", campaignRepository.count());
            } catch (JsonProcessingException e) {
                log.error("Test verisi oluşturulurken JSON hatası!", e);
            }
        } else {
            log.info("Veritabanında zaten kampanya mevcut. Oluşturma atlandı.");
        }
    }
}