package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.output.UserResponse;
import com.backend.couriersyncfeat4.entity.UserEntity;

@Mapper(componentModel = "spring", uses = CatalogMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", source = "roleEntity")
    UserResponse toResponse(UserEntity user);
}
