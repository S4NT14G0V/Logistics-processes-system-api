package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.input.PlaceInput;
import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.PlaceMapper;
import com.backend.couriersyncfeat4.repository.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    public PlaceService(PlaceRepository placeRepository, PlaceMapper placeMapper) {
        this.placeRepository = placeRepository;
        this.placeMapper = placeMapper;
    }

    public PlaceEntity getByUuid(UUID uuid) {
        if (uuid == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Place id is required", HttpStatus.BAD_REQUEST);
        }
        return placeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.PLACE_NOT_FOUND));
    }

    public List<PlaceResponse> findAll() {
        return placeRepository.findAll().stream().map(placeMapper::toResponse).toList();
    }

    public PlaceResponse findById(UUID uuid) {
        return placeMapper.toResponse(getByUuid(uuid));
    }

    public PlaceResponse create(PlaceInput input) {
        PlaceEntity place = new PlaceEntity();
        apply(input, place);
        return placeMapper.toResponse(placeRepository.save(place));
    }

    public PlaceResponse update(UUID uuid, PlaceInput input) {
        PlaceEntity place = getByUuid(uuid);
        apply(input, place);
        return placeMapper.toResponse(placeRepository.save(place));
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
