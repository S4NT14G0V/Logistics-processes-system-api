package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.output.AlertResponse;
import com.backend.couriersyncfeat4.entity.AlertEntity;

@Mapper(componentModel = "spring", uses = CatalogMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AlertMapper {

    @Mapping(target = "alertType", source = "alertTypeEntity")
    @Mapping(target = "packageId", source = "packageEntity.uuid")
    AlertResponse toResponse(AlertEntity alert);
}
