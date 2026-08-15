package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;

import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.entity.PlaceEntity;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    PlaceResponse toResponse(PlaceEntity place);
}
