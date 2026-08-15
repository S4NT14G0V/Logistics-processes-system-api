package com.backend.couriersyncfeat4.mapper;

import java.util.List;

import org.hibernate.Hibernate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusHistoryResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.PackageTrackingResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;

@Mapper(componentModel = "spring", uses = {PlaceMapper.class}, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PackageMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "trackingCode", ignore = true)
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    PackageEntity toEntity(PackageInput input, PlaceEntity origin, PlaceEntity destination);

    @Mapping(target = "history", source = "statusHistory", qualifiedByName = "fullHistory")
    PackageResponse toResponse(PackageEntity entity);

    @Mapping(target = "history", source = "statusHistory", qualifiedByName = "publicHistory")
    PackageTrackingResponse toTrackingResponse(PackageEntity entity);

    PackageStatusResponse toStatusResponse(PackageStatusEntity status);

    @Named("full")
    @Mapping(target = "changedBy", expression = "java(h.getChangedBy() != null ? h.getChangedBy().getEmail() : null)")
    PackageStatusHistoryResponse toHistoryResponse(PackageStatusHistoryEntity h);

    @Named("public")
    @Mapping(target = "changedBy", ignore = true)
    PackageStatusHistoryResponse toPublicHistoryResponse(PackageStatusHistoryEntity h);

    default List<PackageStatusHistoryResponse> toHistoryResponses(List<PackageStatusHistoryEntity> history) {
        return mapFullHistory(history);
    }

    @Named("fullHistory")
    default List<PackageStatusHistoryResponse> mapFullHistory(List<PackageStatusHistoryEntity> history) {
        if (history == null || !Hibernate.isInitialized(history)) {
            return List.of();
        }
        return history.stream().map(this::toHistoryResponse).toList();
    }

    @Named("publicHistory")
    default List<PackageStatusHistoryResponse> mapPublicHistory(List<PackageStatusHistoryEntity> history) {
        if (history == null || !Hibernate.isInitialized(history)) {
            return List.of();
        }
        return history.stream().map(this::toPublicHistoryResponse).toList();
    }
}
