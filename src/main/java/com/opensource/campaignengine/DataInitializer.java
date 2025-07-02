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
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataInitializer(CampaignRepository campaignRepository, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRolesAndUsers();
        createTestCampaigns();
    }

    protected void createRolesAndUsers() {
        log.info("Kullanıcı ve Rol kontrolü yapılıyor...");

        // 1. Rolleri "yoksa ekle" mantığıyla oluştur
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_CAMPAIGN_MANAGER");
        createRoleIfNotFound("ROLE_USER_MANAGER");

        // 2. Admin kullanıcısını "yoksa ekle" mantığıyla oluştur
        if (userRepository.count() == 0) {
            log.info("Hiç kullanıcı bulunamadı. Varsayılan 'admin' kullanıcısı oluşturuluyor...");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setEnabled(true);

            // Admin kullanıcısına tüm rolleri verelim
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

    // Bu yardımcı metot, bir rol veritabanında yoksa onu oluşturur.
    private void createRoleIfNotFound(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("{} rolü oluşturuldu.", roleName);
        }
    }

    // createTestCampaigns metodu aynı kalabilir.
    private void createTestCampaigns() {
        // ...
    }
}