package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.config.Permission;
import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageCountProjection;
import com.backend.couriersyncfeat4.dto.output.PackageCountResponse;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.enums.PackageStatusEnum;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.PackageMapper;
import com.backend.couriersyncfeat4.mapper.PlaceMapper;
import com.backend.couriersyncfeat4.repository.PackageRepository;
import com.backend.couriersyncfeat4.repository.PackageStatusHistoryRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PackageService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final PackageRepository packageRepository;
    private final UserService userService;
    private final PackageStatusService packageStatusService;
    private final PackageStatusHistoryRepository statusHistoryRepository;
    private final PlaceService placeService;
    private final PackageMapper packageMapper;
    private final PlaceMapper placeMapper;

    @Autowired
    public PackageService(PackageRepository packageRepository, UserService userService,
            PackageStatusService packageStatusService, PackageStatusHistoryRepository statusHistoryRepository,
            PlaceService placeService, PackageMapper packageMapper, PlaceMapper placeMapper) {
        this.packageRepository = packageRepository;
        this.userService = userService;
        this.packageStatusService = packageStatusService;
        this.statusHistoryRepository = statusHistoryRepository;
        this.placeService = placeService;
        this.packageMapper = packageMapper;
        this.placeMapper = placeMapper;
    }

    public PackageResponse createPackage(PackageInput input) {
        if (input == null || input.origin() == null || input.destination() == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Origin and destination are required");
        }

        PlaceEntity origin = placeService.getByUuid(input.origin());
        PlaceEntity destination = placeService.getByUuid(input.destination());
        
        if (origin.getUuid().equals(destination.getUuid())) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Origin and destination must be different");
        }

        UserEntity currentUser = userService.getCurrentUser();
        PackageStatusEntity createdStatus = packageStatusService.findByCode(PackageStatusEnum.CREATED.getCode());

        PackageEntity packageEntity = packageMapper.toEntity(input, origin, destination);
        packageEntity.setOwnerUser(currentUser);
        packageEntity.setStatus(createdStatus);

        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, null, createdStatus, currentUser);
        return packageMapper.toResponse(packageEntity);
    }

    public List<PackageResponse> findAllPackages(Integer page, Integer size) {
        Pageable pageable = toPageable(page, size);
        if (SecurityUtils.hasPermission(Permission.PACKAGE_READ_ALL)) {
            return packageRepository.findAll(pageable).stream().map(packageMapper::toResponse).toList();
        }
        return packageRepository.findAllByOwnerUser_Email(SecurityUtils.currentUserEmail(), pageable)
                .stream().map(packageMapper::toResponse).toList();
    }

    public PackageResponse findPackageById(UUID id) {
        PackageEntity packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));
        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_READ_ALL);
        return packageMapper.toResponse(packageEntity);
    }

    public PackageResponse findPackageByTrackingCode(String trackingCode) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Tracking code is null or empty");
        }

        PackageEntity packageEntity = packageRepository.findByTrackingCode(trackingCode.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));

        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_READ_ALL);
        return packageMapper.toResponse(packageEntity);
    }

    public PackageResponse updatePackage(UUID id, PackageInput input) {
        if (input == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Update data is required");
        }

        PackageEntity packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));
        
        assertCanUpdate(packageEntity);

        PlaceEntity origin = null;
        PlaceEntity destination = null;

        if (input.origin() != null) {
            origin = placeService.getByUuid(input.origin());
        }
        if (input.destination() != null) {
            destination = placeService.getByUuid(input.destination());
        }
        if (origin != null && destination != null && origin.getUuid().equals(destination.getUuid())) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Origin and destination must be different");
        }

        packageEntity.updateFrom(input.description(), origin, destination);
        packageRepository.save(packageEntity);
        return packageMapper.toResponse(packageEntity);
    }

    public PackageResponse deletePackageById(UUID id, String reason) {
        PackageEntity packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));
        
        assertCanCancel(packageEntity);

        PackageStatusEntity cancelledStatus = packageStatusService.findByCode(PackageStatusEnum.CANCELLED.getCode());
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(cancelledStatus);
        packageEntity.setCancelledAt(LocalDateTime.now());
        packageEntity.setCancellationReason(reason);

        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, cancelledStatus, userService.getCurrentUser());
        return packageMapper.toResponse(packageEntity);
    }

    public PackageResponse changePackageStatus(UUID id, String statusCode) {
        PackageEntity packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));

        PackageStatusEnum target = PackageStatusEnum.fromCode(statusCode);
        if (target == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Unknown target status: " + statusCode);
        }

        PackageStatusEnum current = PackageStatusEnum.fromCode(packageEntity.getStatus().getCode());
        if (current == null || !current.canTransitionTo(target)) {
            String fromName = current != null ? current.getName() : "unknown";
            throw new ApplicationException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Invalid transition from '" + fromName + "' to '" + target.getName() + "'");
        }

        if (!SecurityUtils.hasPermission(Permission.PACKAGE_UPDATE)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN, "Access denied");
        }

        PackageStatusEntity targetStatus = packageStatusService.findByCode(statusCode);
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(targetStatus);
        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, targetStatus, userService.getCurrentUser());
        return packageMapper.toResponse(packageEntity);
    }

    public List<PackageResponse> findPackagesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<PackageEntity> entities;
        if (startDate != null && endDate != null) {
            entities = packageRepository.findByRegisteredAtBetween(startDate, endDate);
        } else if (startDate != null) {
            entities = packageRepository.findByRegisteredAtAfter(startDate);
        } else if (endDate != null) {
            entities = packageRepository.findByRegisteredAtBefore(endDate);
        } else {
            return Collections.emptyList();
        }
        return entities.stream().map(packageMapper::toResponse).toList();
    }

    public PackageCountResponse findPackageCountByUserId(UUID userId) {
        PackageCountProjection projection = packageRepository.findCountByUserId(userId);
        if (projection == null || projection.getPackageCount() == null) {
            return new PackageCountResponse(userId, 0);
        }
        return new PackageCountResponse(projection.getUserId(), projection.getPackageCount().intValue());
    }

    public List<PackageResponse> findPackagesByStatusIn(List<String> statusCodes) {
        if (statusCodes == null || statusCodes.isEmpty()) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Status list cannot be null or empty");
        }
        List<PackageStatusEntity> statusEntities = packageStatusService.findByCodeIn(statusCodes);
        return packageRepository.findByStatusIn(statusEntities).stream().map(packageMapper::toResponse).toList();
    }

    public List<PackageCountResponse> findCountByAllUsers() {
        return packageRepository.findCountByAllUsers().stream()
                .map(projection -> new PackageCountResponse(projection.getUserId(),
                        projection.getPackageCount() == null ? 0 : projection.getPackageCount().intValue()))
                .toList();
    }

    public List<PackageResponse> findAllPackagesByUserId(UUID userId) {
        return packageRepository.findAllByOwnerUser_Id(userId).stream().map(packageMapper::toResponse).toList();
    }

    public List<PackageResponse> findAllPackagesByUbication(UUID origin, UUID destination) {
        List<PackageEntity> entities;
        if (origin == null && destination == null) {
            return Collections.emptyList();
        } else if (origin == null) {
            entities = packageRepository.findAllByDestination_Uuid(destination);
        } else if (destination == null) {
            entities = packageRepository.findAllByOrigin_Uuid(origin);
        } else {
            entities = packageRepository.findAllByOrigin_UuidAndDestination_Uuid(origin, destination);
        }
        return entities.stream().map(packageMapper::toResponse).toList();
    }

    private void assertCanUpdate(PackageEntity packageEntity) {
        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_UPDATE);
        if (!packageEntity.isCreated()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_UPDATABLE,
                    "Package can only be updated while in 'Created' status");
        }
    }

    private void assertCanCancel(PackageEntity packageEntity) {
        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_CANCEL);
        if (packageEntity.isDelivered()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_CANCELLABLE, "A delivered package cannot be cancelled");
        }
        if (packageEntity.isCancelled()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_CANCELLABLE, "Package is already cancelled");
        }
    }

    private void assertOwnerOrPermission(PackageEntity packageEntity, Permission permission) {
        boolean isOwner = packageEntity.getOwnerUser() != null
                && packageEntity.getOwnerUser().getEmail().equals(SecurityUtils.currentUserEmail());
        if (!isOwner && !SecurityUtils.hasPermission(permission)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN, "Access denied");
        }
    }

    private void recordStatusChange(PackageEntity packageEntity, PackageStatusEntity from,
            PackageStatusEntity to, UserEntity changedBy) {
        PackageStatusHistoryEntity history = new PackageStatusHistoryEntity();
        history.setPackageEntity(packageEntity);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(changedBy);
        statusHistoryRepository.save(history);
    }

    private Pageable toPageable(Integer page, Integer size) {
        int pageNum = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(pageNum, pageSize);
    }
}
