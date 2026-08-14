package com.backend.couriersyncfeat4.mapper;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import org.springframework.stereotype.Component;

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
                toStatusResponse(entity.getStatus()),
                placeMapper.toResponse(entity.getOrigin()),
                placeMapper.toResponse(entity.getDestination()));
    }

    private PackageStatusResponse toStatusResponse(PackageStatusEntity status) {
        if (status == null) {
            return null;
        }
        return new PackageStatusResponse(status.getId(), status.getCode(), status.getName(), status.getDescription());
    }
}
