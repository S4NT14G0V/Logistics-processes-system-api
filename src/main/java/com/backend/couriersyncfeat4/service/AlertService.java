package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.config.Permission;
import com.backend.couriersyncfeat4.dto.output.AlertResponse;
import com.backend.couriersyncfeat4.entity.AlertEntity;
import com.backend.couriersyncfeat4.entity.AlertTypeEntity;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.AlertMapper;
import com.backend.couriersyncfeat4.repository.AlertRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertTypeService alertTypeService;
    private final UserService userService;
    private final PackageService packageService;
    private final AlertMapper alertMapper;

    public AlertService(AlertTypeService alertTypeService, AlertRepository alertRepository,
                        UserService userService, PackageService packageService, AlertMapper alertMapper) {
        this.alertTypeService = alertTypeService;
        this.alertRepository = alertRepository;
        this.userService = userService;
        this.packageService = packageService;
        this.alertMapper = alertMapper;
    }

    public AlertEntity createAlert(UUID userId, UUID packageId, int alertTypeId, String description) {
        UserEntity userEntity = userService.getById(userId);
        PackageEntity packageEntity = packageService.getPackageById(packageId);
        AlertTypeEntity alertTypeEntity = alertTypeService.findById(alertTypeId);

        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setUser(userEntity);
        alertEntity.setPackageEntity(packageEntity);
        alertEntity.setAlertTypeEntity(alertTypeEntity);
        alertEntity.setDescription(description);
        alertEntity.setRegisteredAt(LocalDateTime.now());
        return alertRepository.save(alertEntity);
    }

    public List<AlertResponse> findAll() {
        return alertRepository.findAllOrderByRegisteredAtDesc().stream()
                .map(alertMapper::toResponse).toList();
    }

    public List<AlertResponse> findAllAlertsByUserId(UUID userId) {
        if (!SecurityUtils.hasPermission(Permission.ALERT_READ_ALL)) {
            UserEntity current = userService.getCurrentUser();
            if (current == null || !current.getId().equals(userId)) {
                throw new ApplicationException(ErrorCodes.FORBIDDEN);
            }
        }
        return alertRepository.findAllByUserId(userId).stream()
                .map(alertMapper::toResponse).toList();
    }
}
