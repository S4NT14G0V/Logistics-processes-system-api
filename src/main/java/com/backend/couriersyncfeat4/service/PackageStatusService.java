package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.repository.PackageStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageStatusService {

    private final PackageStatusRepository packageStatusRepository;

    @Autowired
    public PackageStatusService(PackageStatusRepository packageStatusRepository) {
        this.packageStatusRepository = packageStatusRepository;
    }

    public List<PackageStatusEntity> findAll() {
        return packageStatusRepository.findAll();
    }

    public PackageStatusEntity findById(int id) {
        return packageStatusRepository.findById(id).orElseThrow(()->new RuntimeException("Package Status not found"));
    }
}
