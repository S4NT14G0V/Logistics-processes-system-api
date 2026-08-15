package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.repository.PackageStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageStatusService {

    private final PackageStatusRepository packageStatusRepository;

    public PackageStatusService(PackageStatusRepository packageStatusRepository) {
        this.packageStatusRepository = packageStatusRepository;
    }

    public List<PackageStatusEntity> findAll() {
        return packageStatusRepository.findAll();
    }

    public PackageStatusEntity findById(int id) {
        return packageStatusRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_STATUS_NOT_FOUND,
                        "Package status not found: " + id));
    }

    public PackageStatusEntity findByCode(String code) {
        return packageStatusRepository.findByCode(code)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_STATUS_NOT_FOUND,
                        "Package status not found: " + code));
    }

    public List<PackageStatusEntity> findByCodeIn(List<String> codes) {
        return packageStatusRepository.findByCodeIn(codes);
    }
}
