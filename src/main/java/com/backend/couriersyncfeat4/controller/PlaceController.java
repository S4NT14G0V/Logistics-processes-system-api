package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.PlaceInput;
import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PlaceResponse> findAllPlaces() {
        return placeService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PlaceResponse findPlaceByUuid(@Argument UUID uuid) {
        return placeService.findById(uuid);
    }

    @PreAuthorize("hasAuthority('package:create:all')")
    @MutationMapping
    public PlaceResponse createPlace(@Argument("input") @Valid PlaceInput input) {
        return placeService.create(input);
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PlaceResponse updatePlace(@Argument UUID uuid, @Argument("input") @Valid PlaceInput input) {
        return placeService.update(uuid, input);
    }

    @PreAuthorize("hasAuthority('package:cancel:all')")
    @MutationMapping
    public boolean deletePlace(@Argument UUID uuid) {
        return placeService.delete(uuid);
    }
}
