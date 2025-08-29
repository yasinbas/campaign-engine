package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Role;
import com.opensource.campaignengine.domain.User;
import com.opensource.campaignengine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService; // Rolleri bulmak için RoleService eklendi

    public UserService(UserRepository userRepository, RoleService roleService) { // Constructor güncellendi
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public List<User> findAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    // YENİ METOT: ID'ye göre tek bir kullanıcı bulur
    public Optional<User> findById(Long id) {
        log.debug("Finding user with id {}", id);
        return userRepository.findById(id);
    }

    // YENİ METOT: Bir kullanıcının rollerini günceller
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // Kullanıcıyı bul, bulamazsan hata fırlat
        log.info("Updating roles for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz Kullanıcı Id:" + userId));

        // Önce kullanıcının mevcut rollerini temizle
        user.getRoles().clear();

        // Eğer formdan yeni rol ID'leri geldiyse
        if (roleIds != null && !roleIds.isEmpty()) {
            // Gelen ID'lere karşılık gelen Role nesnelerini veritabanından bul
            List<Role> newRoles = roleService.findAllByIds(roleIds);
            log.debug("Assigning roles {} to user {}", roleIds, userId);
            // Kullanıcının rolleri olarak bu yeni listeyi ata
            user.setRoles(new HashSet<>(newRoles));
        }

        // Değişiklikleri kaydet
        userRepository.save(user);
        log.info("Roles updated for user {}", userId);
    }
}