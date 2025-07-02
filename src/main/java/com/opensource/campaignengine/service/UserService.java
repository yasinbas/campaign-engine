package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Role;
import com.opensource.campaignengine.domain.User;
import com.opensource.campaignengine.repository.RoleRepository;
import com.opensource.campaignengine.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Rolleri bulmak için eklendi

    public UserService(UserRepository userRepository, RoleRepository roleRepository) { // Constructor güncellendi
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // YENİ METOT: ID'ye göre tek bir kullanıcı bulur
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // YENİ METOT: Bir kullanıcının rollerini günceller
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // Kullanıcıyı bul, bulamazsan hata fırlat
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz Kullanıcı Id:" + userId));

        // Önce kullanıcının mevcut rollerini temizle
        user.getRoles().clear();

        // Eğer formdan yeni rol ID'leri geldiyse
        if (roleIds != null && !roleIds.isEmpty()) {
            // Gelen ID'lere karşılık gelen Role nesnelerini veritabanından bul
            List<Role> newRoles = roleRepository.findAllById(roleIds);
            // Kullanıcının rolleri olarak bu yeni listeyi ata
            user.setRoles(new HashSet<>(newRoles));
        }

        // Değişiklikleri kaydet
        userRepository.save(user);
    }
}