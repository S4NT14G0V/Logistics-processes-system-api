package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.output.LocationResponse;
import com.backend.couriersyncfeat4.entity.LocationEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    LocationResponse toResponse(LocationEntity location);
}
