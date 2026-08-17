package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleEntity> findAll() {
        return roleRepository.findAll();
    }

    public RoleEntity findById(int id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.ROLE_NOT_FOUND, "Role not found: " + id));
    }

    public RoleEntity findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.ROLE_NOT_FOUND, "Role not found: " + name));
    }
}
