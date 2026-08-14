package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.repository.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceEntity getByUuid(UUID uuid) {
        if (uuid == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Place id is required", HttpStatus.BAD_REQUEST);
        }
        return placeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.RESOURCE_NOT_FOUND, "Place not found",
                        HttpStatus.NOT_FOUND));
    }
}
