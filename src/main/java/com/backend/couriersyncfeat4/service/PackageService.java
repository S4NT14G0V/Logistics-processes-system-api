package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.config.Permission;
import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.input.PackageUpdateInput;
import com.backend.couriersyncfeat4.dto.output.PackageCountProjection;
import com.backend.couriersyncfeat4.dto.output.PackageCountResponse;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatsResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusHistoryResponse;
import com.backend.couriersyncfeat4.dto.output.PackageTrackingResponse;
import com.backend.couriersyncfeat4.dto.output.StatusCount;
import com.backend.couriersyncfeat4.dto.output.UserPackageCount;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.enums.PackageStatusEnum;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.PackageMapper;
import com.backend.couriersyncfeat4.repository.PackageRepository;
import com.backend.couriersyncfeat4.repository.PackageStatusHistoryRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import com.backend.couriersyncfeat4.sse.SseEmitterService;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PackageService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final PackageRepository packageRepository;
    private final UserService userService;
    private final PackageStatusService packageStatusService;
    private final PackageStatusHistoryRepository statusHistoryRepository;
    private final PlaceService placeService;
    private final PackageMapper packageMapper;
    private final PricingService pricingService;
    private final SseEmitterService sseEmitterService;

    public PackageResponse createPackage(PackageInput input) {
        PlaceEntity origin = placeService.getByUuid(input.origin());
        PlaceEntity destination = placeService.getByUuid(input.destination());

        if (origin.getUuid().equals(destination.getUuid())) {
            throw new ApplicationException(ErrorCodes.SAME_VALUE);
        }

        UserEntity owner = input.ownerUserId() != null
                ? userService.getById(input.ownerUserId())
                : userService.getCurrentUser();

        PackageStatusEntity createdStatus = packageStatusService.findByCode(PackageStatusEnum.CREATED.getCode());
        PackageEntity packageEntity = packageMapper.toEntity(input, origin, destination);
        packageEntity.setOwnerUser(owner);
        packageEntity.setStatus(createdStatus);
        packageEntity.setTrackingCode(assignUniqueTrackingCode());
        applyPricing(packageEntity);

        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, null, createdStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.created",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse proposePackage(PackageInput input) {
        PlaceEntity origin = placeService.getByUuid(input.origin());
        PlaceEntity destination = placeService.getByUuid(input.destination());

        if (origin.getUuid().equals(destination.getUuid())) {
            throw new ApplicationException(ErrorCodes.SAME_VALUE);
        }

        UserEntity currentUser = userService.getCurrentUser();
        PackageStatusEntity proposedStatus = packageStatusService.findByCode(PackageStatusEnum.PROPOSED.getCode());
        PackageEntity packageEntity = packageMapper.toEntity(input, origin, destination);
        packageEntity.setOwnerUser(currentUser);
        packageEntity.setStatus(proposedStatus);
        packageEntity.setTrackingCode(assignUniqueTrackingCode());
        applyPricing(packageEntity);

        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, null, proposedStatus, currentUser);
        return publish(packageEntity.getOwnerUser().getId(), "package.proposed",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse approvePackage(UUID id) {
        assertCanUpdateAll();
        PackageEntity packageEntity = getPackageById(id);
        if (!packageEntity.isProposed()) {
            throw new ApplicationException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Only a proposed package can be approved");
        }

        PackageStatusEntity createdStatus = packageStatusService.findByCode(PackageStatusEnum.CREATED.getCode());
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(createdStatus);
        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, createdStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.approved",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse rejectPackage(UUID id, String reason) {
        assertCanUpdateAll();
        PackageEntity packageEntity = getPackageById(id);
        if (!packageEntity.isProposed()) {
            throw new ApplicationException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Only a proposed package can be rejected");
        }

        PackageStatusEntity cancelledStatus = packageStatusService.findByCode(PackageStatusEnum.CANCELLED.getCode());
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(cancelledStatus);
        packageEntity.setCancelledAt(LocalDateTime.now());
        packageEntity.setCancellationReason(reason);
        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, cancelledStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.rejected",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse reactivatePackage(UUID id) {
        assertCanUpdateAll();
        PackageEntity packageEntity = getPackageById(id);
        if (!packageEntity.isCancelled()) {
            throw new ApplicationException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Only a cancelled package can be reactivated");
        }

        PackageStatusEntity createdStatus = packageStatusService.findByCode(PackageStatusEnum.CREATED.getCode());
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(createdStatus);
        packageEntity.setCancelledAt(null);
        packageEntity.setCancellationReason(null);
        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, createdStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.reactivated",
                packageMapper.toResponse(packageEntity));
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
        PackageEntity packageEntity = getPackageById(id);
        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_READ_ALL);
        return packageMapper.toResponse(packageEntity);
    }

    public PackageEntity getPackageById(UUID id) {
        if (id == null) throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Package id is required");
        return packageRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));
    }

    public List<PackageStatusHistoryResponse> findPackageHistory(UUID packageId) {
        PackageEntity packageEntity = getPackageById(packageId);
        assertOwnerOrPermission(packageEntity, Permission.PACKAGE_READ_ALL);
        return packageMapper.toHistoryResponses(
                statusHistoryRepository.findAllByPackageEntity_UuidOrderByChangedAtAsc(packageId));
    }

    public PackageTrackingResponse findPackageByTrackingCode(String trackingCode) {
        return packageMapper.toTrackingResponse(getByTrackingCode(trackingCode));
    }

    public PackageEntity getByTrackingCode(String trackingCode) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Tracking code is null or empty");
        }
        return packageRepository.findByTrackingCode(trackingCode.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PACKAGE_NOT_FOUND, "Package not found"));
    }

    public PackageResponse updatePackage(UUID id, PackageUpdateInput input) {
        PackageEntity packageEntity = getPackageById(id);
        assertCanUpdate(packageEntity);

        PlaceEntity origin = input.origin() != null ? placeService.getByUuid(input.origin()) : null;
        PlaceEntity destination = input.destination() != null ? placeService.getByUuid(input.destination()) : null;

        if (origin != null && destination != null && origin.getUuid().equals(destination.getUuid())) {
            throw new ApplicationException(ErrorCodes.SAME_VALUE);
        }

        packageEntity.updateFrom(input.description(), origin, destination);
        if (origin != null || destination != null) {
            applyPricing(packageEntity);
        }
        packageRepository.save(packageEntity);
        return publish(packageEntity.getOwnerUser().getId(), "package.updated",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse cancelPackage(UUID id, String reason) {
        PackageEntity packageEntity = getPackageById(id);
        assertCanCancel(packageEntity);

        PackageStatusEntity cancelledStatus = packageStatusService.findByCode(PackageStatusEnum.CANCELLED.getCode());
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(cancelledStatus);
        packageEntity.setCancelledAt(LocalDateTime.now());
        packageEntity.setCancellationReason(reason);

        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, cancelledStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.cancelled",
                packageMapper.toResponse(packageEntity));
    }

    public PackageResponse changePackageStatus(UUID id, String statusCode) {
        assertCanUpdateAll();

        PackageEntity packageEntity = getPackageById(id);

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

        PackageStatusEntity targetStatus = packageStatusService.findByCode(statusCode);
        PackageStatusEntity fromStatus = packageEntity.getStatus();

        packageEntity.setStatus(targetStatus);
        packageRepository.save(packageEntity);
        recordStatusChange(packageEntity, fromStatus, targetStatus, userService.getCurrentUser());
        return publish(packageEntity.getOwnerUser().getId(), "package.status-changed",
                packageMapper.toResponse(packageEntity));
    }

    public List<PackageResponse> findPackagesByDateRange(Integer page, Integer size,
            LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = toPageable(page, size);
        List<PackageEntity> entities;
        if (startDate != null && endDate != null) {
            entities = packageRepository.findByRegisteredAtBetween(startDate, endDate, pageable).getContent();
        } else if (startDate != null) {
            entities = packageRepository.findByRegisteredAtAfter(startDate, pageable).getContent();
        } else if (endDate != null) {
            entities = packageRepository.findByRegisteredAtBefore(endDate, pageable).getContent();
        } else {
            return Collections.emptyList();
        }
        return filterOwned(entities).stream().map(packageMapper::toResponse).toList();
    }

    public PackageCountResponse findPackageCountByUserId(UUID userId) {
        assertOwnerOrPermissionByUserId(userId, Permission.PACKAGE_READ_ALL);
        userService.getById(userId);
        PackageCountProjection projection = packageRepository.findCountByUserId(userId);
        if (projection == null || projection.getPackageCount() == null) {
            return new PackageCountResponse(userId, 0);
        }
        return new PackageCountResponse(projection.getUserId(), projection.getPackageCount().intValue());
    }

    public List<PackageResponse> findPackagesByStatusIn(Integer page, Integer size, List<String> statusCodes) {
        Pageable pageable = toPageable(page, size);
        List<PackageEntity> entities;
        if (statusCodes == null || statusCodes.isEmpty()) {
            entities = packageRepository.findAll(pageable).getContent();
        } else {
            List<PackageStatusEntity> statusEntities = packageStatusService.findByCodeIn(statusCodes);
            entities = packageRepository.findByStatusIn(statusEntities, pageable).getContent();
        }
        return filterOwned(entities).stream().map(packageMapper::toResponse).toList();
    }

    public PackageStatsResponse findPackageCountByAllUsers() {
        List<UserPackageCount> users = packageRepository.findCountByAllUsers().stream()
                .map(p -> new UserPackageCount(p.getUserId(),
                        p.getPackageCount() == null ? 0 : p.getPackageCount().intValue()))
                .toList();
        int total = (int) packageRepository.count();
        return new PackageStatsResponse(users, total);
    }

    public List<StatusCount> findPackageCountByAllStatus() {
        return packageRepository.findCountByStatus().stream()
                .map(s -> new StatusCount(s.getStatusCode(), s.getCount() == null ? 0 : s.getCount().intValue()))
                .toList();
    }

    public List<PackageResponse> findAllPackagesByUserId(Integer page, Integer size, UUID userId) {
        assertOwnerOrPermissionByUserId(userId, Permission.PACKAGE_READ_ALL);
        userService.getById(userId);
        Pageable pageable = toPageable(page, size);
        return packageRepository.findAllByOwnerUser_Id(userId, pageable).getContent()
                .stream().map(packageMapper::toResponse).toList();
    }

    public List<PackageResponse> findAllPackagesByPlace(Integer page, Integer size, UUID origin, UUID destination) {
        Pageable pageable = toPageable(page, size);
        List<PackageEntity> entities;
        if (origin == null && destination == null) {
            return Collections.emptyList();
        } else if (origin == null) {
            entities = packageRepository.findAllByDestination_Uuid(destination, pageable).getContent();
        } else if (destination == null) {
            entities = packageRepository.findAllByOrigin_Uuid(origin, pageable).getContent();
        } else {
            entities = packageRepository.findAllByOrigin_UuidAndDestination_Uuid(origin, destination, pageable).getContent();
        }
        return filterOwned(entities).stream().map(packageMapper::toResponse).toList();
    }

    private PackageResponse publish(UUID ownerId, String eventName, PackageResponse response) {
        sseEmitterService.send(ownerId, eventName, response);
        return response;
    }

    private void applyPricing(PackageEntity packageEntity) {
        double distance = pricingService.distanceKm(packageEntity.getOrigin(), packageEntity.getDestination());
        packageEntity.setDistanceKm(distance);
        packageEntity.setPrice(pricingService.calculatePrice(packageEntity.getWeightKg(), distance));
    }

    private String assignUniqueTrackingCode() {
        String code;
        do {
            code = PackageEntity.generateTrackingCode();
        } while (packageRepository.existsByTrackingCode(code));
        return code;
    }

    private void assertCanUpdate(PackageEntity packageEntity) {
        assertAllOrOwn(packageEntity, Permission.PACKAGE_UPDATE_ALL, Permission.PACKAGE_UPDATE_OWN);
        if (!packageEntity.isCreated()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_UPDATABLE,
                    "Package can only be updated while in 'Created' status");
        }
    }

    private void assertCanUpdateAll() {
        if (!SecurityUtils.hasPermission(Permission.PACKAGE_UPDATE_ALL)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN);
        }
    }

    private void assertCanCancel(PackageEntity packageEntity) {
        assertAllOrOwn(packageEntity, Permission.PACKAGE_CANCEL_ALL, Permission.PACKAGE_CANCEL_OWN);
        if (packageEntity.isDelivered()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_CANCELLABLE, "A delivered package cannot be cancelled");
        }
        if (packageEntity.isCancelled()) {
            throw new ApplicationException(ErrorCodes.PACKAGE_NOT_CANCELLABLE, "Package is already cancelled");
        }
    }

    private void assertOwnerOrPermissionByUserId(UUID userId, Permission permission) {
        if (!SecurityUtils.hasPermission(Permission.PACKAGE_READ_ALL)) {
            UserEntity currentUser = userService.getCurrentUser();
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                throw new ApplicationException(ErrorCodes.FORBIDDEN);
            }
        }
    }

    private void assertOwnerOrPermission(PackageEntity packageEntity, Permission permission) {
        if (!isOwner(packageEntity) && !SecurityUtils.hasPermission(permission)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN);
        }
    }

    private void assertAllOrOwn(PackageEntity packageEntity, Permission allPermission, Permission ownPermission) {
        if (!SecurityUtils.hasPermission(allPermission)
                && !(SecurityUtils.hasPermission(ownPermission) && isOwner(packageEntity))) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN);
        }
    }

    private boolean isOwner(PackageEntity packageEntity) {
        return packageEntity.getOwnerUser() != null
                && packageEntity.getOwnerUser().getEmail().equals(SecurityUtils.currentUserEmail());
    }

    private List<PackageEntity> filterOwned(List<PackageEntity> entities) {
        if (SecurityUtils.hasPermission(Permission.PACKAGE_READ_ALL)) {
            return entities;
        }
        return entities.stream().filter(this::isOwner).toList();
    }

    private void recordStatusChange(PackageEntity packageEntity, PackageStatusEntity from,
            PackageStatusEntity to, UserEntity changedBy) {
        PackageStatusHistoryEntity history = new PackageStatusHistoryEntity();
        history.setPackageEntity(packageEntity);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(changedBy);
        history.setDescription(buildTransitionDescription(from, to));
        statusHistoryRepository.save(history);

        List<PackageStatusHistoryEntity> entries = new ArrayList<>();
        if (packageEntity.getStatusHistory() != null) {
            entries.addAll(packageEntity.getStatusHistory());
        }
        entries.add(history);
        packageEntity.setStatusHistory(entries);
    }

    private String buildTransitionDescription(PackageStatusEntity from, PackageStatusEntity to) {
        String fromCode = from != null ? from.getCode() : null;
        String toCode = to != null ? to.getCode() : null;
        if ("CREATED".equals(toCode)) {
            if (fromCode == null) {
                return "Paquete creado";
            }
            if ("PROPOSED".equals(fromCode)) {
                return "Propuesta aprobada";
            }
            if ("CANCELLED".equals(fromCode)) {
                return "Paquete reactivado";
            }
            return "Paquete en bodega";
        }
        if ("PROPOSED".equals(toCode)) {
            return "Propuesta de envío creada";
        }
        if ("IN_TRANSIT".equals(toCode)) {
            return "Paquete en transporte";
        }
        if ("DELIVERED".equals(toCode)) {
            return "Paquete entregado";
        }
        if ("CANCELLED".equals(toCode)) {
            return "PROPOSED".equals(fromCode) ? "Propuesta rechazada" : "Paquete cancelado";
        }
        return "Cambio de estado";
    }

    private Pageable toPageable(Integer page, Integer size) {
        int pageNum = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(pageNum, pageSize);
    }
}
