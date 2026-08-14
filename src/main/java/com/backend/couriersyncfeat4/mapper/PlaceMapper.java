package com.backend.couriersyncfeat4.mapper;

import org.springframework.stereotype.Component;

import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.entity.PlaceEntity;

@Component
public class PlaceMapper {

    public PlaceResponse toResponse(PlaceEntity place) {
        return new PlaceResponse(
            place.getUuid(),
            place.getName(),
            place.getAddress(),
            place.getCity(),
            place.getDepartment(),
            place.getLatitude(),
            place.getLongitude());
    }
}
