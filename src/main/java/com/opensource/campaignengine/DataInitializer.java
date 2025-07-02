package com.opensource.campaignengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensource.campaignengine.domain.*;
import com.opensource.campaignengine.repository.CampaignRepository;
import com.opensource.campaignengine.repository.CategoryRepository;
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
    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;

    public DataInitializer(CampaignRepository campaignRepository, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, CategoryRepository categoryRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRolesAndUsers();
        createTestCategories();
        createTestCampaigns(); // Artık bu metot da dolu
    }

    protected void createRolesAndUsers() {
        log.info("Kullanıcı ve Rol kontrolü yapılıyor...");
        // Bu metot doğru, dokunmuyoruz.
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_CAMPAIGN_MANAGER");
        createRoleIfNotFound("ROLE_USER_MANAGER");

        if (userRepository.count() == 0) {
            log.info("Hiç kullanıcı bulunamadı. Varsayılan 'admin' kullanıcısı oluşturuluyor...");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setEnabled(true);
            Set<Role> adminRoles = Set.of(
                    roleRepository.findByName("ROLE_ADMIN"),
                    roleRepository.findByName("ROLE_CAMPAIGN_MANAGER"),
                    roleRepository.findByName("ROLE_USER_MANAGER")
            );
            adminUser.setRoles(adminRoles);
            userRepository.save(adminUser);
            log.info("Varsayılan 'admin' kullanıcısı (şifre: password) ve tüm roller oluşturuldu.");
        }
    }

    private void createRoleIfNotFound(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("{} rolü oluşturuldu.", roleName);
        }
    }

    private void createTestCategories() {
        log.info("Kategori kontrolü yapılıyor...");
        if (categoryRepository.count() == 0) {
            log.info("Hiç kategori bulunamadı. Varsayılan kategoriler oluşturuluyor...");
            Category cat1 = new Category();
            cat1.setName("İçecekler");

            Category cat2 = new Category();
            cat2.setName("Atıştırmalıklar");

            Category cat3 = new Category();
            cat3.setName("Elektronik");

            categoryRepository.saveAll(List.of(cat1, cat2, cat3));
            log.info("Varsayılan kategoriler oluşturuldu.");
        }
    }

    // --- BU METODUN İÇİ DOLDURULDU ---
    private void createTestCampaigns() {
        log.info("Kampanya kontrolü yapılıyor...");
        if (campaignRepository.count() == 0) {
            log.info("Veritabanı boş. Test kampanyaları oluşturuluyor...");
            try {
                Campaign campaign1 = new Campaign();
                campaign1.setName("Gazozlarda 3 Al 2 Öde");
                campaign1.setCampaignType(CampaignType.BUY_X_PAY_Y);
                Map<String, Object> details1 = Map.of("productId", "GAZOZ-123", "buyQuantity", 3, "payQuantity", 2);
                campaign1.setDetails(objectMapper.writeValueAsString(details1));
                campaign1.setStartDate(LocalDateTime.now().minusDays(1));
                campaign1.setEndDate(LocalDateTime.now().plusMonths(1));
                campaign1.setActive(true);
                campaign1.setPriority(10);

                Campaign campaign2 = new Campaign();
                campaign2.setName("500 TL Üzeri Sepete %10 İndirim");
                campaign2.setCampaignType(CampaignType.BASKET_TOTAL_PERCENTAGE_DISCOUNT);
                Map<String, Object> details2 = Map.of("minAmount", 500.0, "discountPercentage", 10.0);
                campaign2.setDetails(objectMapper.writeValueAsString(details2));
                campaign2.setStartDate(LocalDateTime.now().minusDays(1));
                campaign2.setEndDate(LocalDateTime.now().plusDays(15));
                campaign2.setActive(true);
                campaign2.setPriority(5);

                campaignRepository.saveAll(List.of(campaign1, campaign2));
                log.info("{} adet test kampanyası veritabanına eklendi.", campaignRepository.count());
            } catch (JsonProcessingException e) {
                log.error("Test verisi oluşturulurken JSON hatası!", e);
            }
        } else {
            log.info("Veritabanında zaten kampanya mevcut. Oluşturma atlandı.");
        }
    }
}