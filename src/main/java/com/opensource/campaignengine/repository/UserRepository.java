package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}