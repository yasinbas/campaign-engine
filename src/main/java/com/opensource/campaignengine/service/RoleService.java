package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Role;
import com.opensource.campaignengine.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        log.debug("Fetching all roles");
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        log.debug("Finding role with id {}", id);
        return roleRepository.findById(id);
    }

    public List<Role> findAllByIds(List<Long> ids) {
        log.debug("Finding roles with ids {}", ids);
        return roleRepository.findAllById(ids);
    }

    public Role save(Role role) {
        log.info("Saving role {}", role.getName());
        return roleRepository.save(role);
    }

    public void deleteById(Long id) {
        log.info("Deleting role with id {}", id);
        roleRepository.deleteById(id);
    }
}
