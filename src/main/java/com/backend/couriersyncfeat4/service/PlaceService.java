package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.input.PlaceInput;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.repository.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<PlaceEntity> findAll() {
        return placeRepository.findAll();
    }

    public PlaceEntity findById(UUID uuid) {
        return placeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PLACE_NOT_FOUND, "Place not found"));
    }

    public PlaceEntity create(PlaceInput input) {
        PlaceEntity place = new PlaceEntity();
        apply(input, place);
        return placeRepository.save(place);
    }

    public PlaceEntity update(UUID uuid, PlaceInput input) {
        PlaceEntity place = getByUuid(uuid);
        apply(input, place);
        return placeRepository.save(place);
    }

    public boolean delete(UUID uuid) {
        if (!placeRepository.existsById(uuid)) {
            throw new ApplicationException(ErrorCodes.PLACE_NOT_FOUND, "Place not found");
        }
        placeRepository.deleteById(uuid);
        return true;
    }

    private void apply(PlaceInput input, PlaceEntity place) {
        place.setName(input.name());
        place.setAddress(input.address());
        place.setCity(input.city());
        place.setDepartment(input.department());
        place.setLatitude(input.latitude());
        place.setLongitude(input.longitude());
    }
}
