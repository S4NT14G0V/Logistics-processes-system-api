package com.backend.couriersyncfeat4.mapper;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusHistoryResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.PackageTrackingResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PackageMapper {

    private final PlaceMapper placeMapper;

    public PackageMapper(PlaceMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    public PackageEntity toEntity(PackageInput input, PlaceEntity origin, PlaceEntity destination) {
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDescription(input.description());
        packageEntity.setOrigin(origin);
        packageEntity.setDestination(destination);
        packageEntity.setWeightKg(input.weightKg());
        packageEntity.setLengthCm(input.lengthCm());
        packageEntity.setWidthCm(input.widthCm());
        packageEntity.setHeightCm(input.heightCm());
        packageEntity.setDeclaredValue(input.declaredValue());
        return packageEntity;
    }

    public PackageResponse toResponse(PackageEntity entity) {
        return new PackageResponse(
                entity.getUuid(),
                entity.getTrackingCode(),
                entity.getDescription(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getCancelledAt(),
                entity.getCancellationReason(),
                entity.getWeightKg(),
                entity.getLengthCm(),
                entity.getWidthCm(),
                entity.getHeightCm(),
                entity.getDistanceKm(),
                entity.getDeclaredValue(),
                entity.getPrice(),
                toStatusResponse(entity.getStatus()),
                placeMapper.toResponse(entity.getOrigin()),
                placeMapper.toResponse(entity.getDestination()),
                toHistoryResponses(entity.getStatusHistory()));
    }

    public PackageTrackingResponse toTrackingResponse(PackageEntity entity) {
        return new PackageTrackingResponse(
                entity.getTrackingCode(),
                entity.getDescription(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getCancelledAt(),
                toStatusResponse(entity.getStatus()),
                placeMapper.toResponse(entity.getOrigin()),
                placeMapper.toResponse(entity.getDestination()),
                toPublicHistoryResponses(entity.getStatusHistory()));
    }

    public List<PackageStatusHistoryResponse> toHistoryResponses(List<PackageStatusHistoryEntity> history) {
        if (history == null || !Hibernate.isInitialized(history)) {
            return List.of();
        }
        return history.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private List<PackageStatusHistoryResponse> toPublicHistoryResponses(List<PackageStatusHistoryEntity> history) {
        if (history == null || !Hibernate.isInitialized(history)) {
            return List.of();
        }
        return history.stream()
                .map(h -> new PackageStatusHistoryResponse(
                        h.getChangedAt(),
                        toStatusResponse(h.getFromStatus()),
                        toStatusResponse(h.getToStatus()),
                        null))
                .toList();
    }

    private PackageStatusHistoryResponse toHistoryResponse(PackageStatusHistoryEntity history) {
        return new PackageStatusHistoryResponse(
                history.getChangedAt(),
                toStatusResponse(history.getFromStatus()),
                toStatusResponse(history.getToStatus()),
                history.getChangedBy() != null ? history.getChangedBy().getEmail() : null);
    }

    private PackageStatusResponse toStatusResponse(PackageStatusEntity status) {
        if (status == null) {
            return null;
        }
        return new PackageStatusResponse(status.getId(), status.getCode(), status.getName(), status.getDescription());
    }
}
