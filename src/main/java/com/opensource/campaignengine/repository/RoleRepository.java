package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}