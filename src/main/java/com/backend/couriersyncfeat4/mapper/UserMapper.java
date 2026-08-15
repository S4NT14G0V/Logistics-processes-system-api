package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.output.UserResponse;
import com.backend.couriersyncfeat4.entity.UserEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(UserEntity user);
}
