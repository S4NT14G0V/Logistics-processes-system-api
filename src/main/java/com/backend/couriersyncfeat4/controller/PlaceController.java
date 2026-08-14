package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.PlaceInput;
import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.mapper.PlaceMapper;
import com.backend.couriersyncfeat4.service.PlaceService;
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
    private final PlaceMapper placeMapper;

    public PlaceController(PlaceService placeService, PlaceMapper placeMapper) {
        this.placeService = placeService;
        this.placeMapper = placeMapper;
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PlaceResponse> findAllPlaces() {
        return placeService.findAll().stream().map(placeMapper::toResponse).toList();
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PlaceResponse findPlaceByUuid(@Argument UUID uuid) {
        return placeMapper.toResponse(placeService.findById(uuid));
    }

    @PreAuthorize("hasAuthority('package:create:all')")
    @MutationMapping
    public PlaceResponse createPlace(@Argument("input") PlaceInput input) {
        return placeMapper.toResponse(placeService.create(input));
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PlaceResponse updatePlace(@Argument UUID uuid, @Argument("input") PlaceInput input) {
        return placeMapper.toResponse(placeService.update(uuid, input));
    }

    @PreAuthorize("hasAuthority('package:cancel:all')")
    @MutationMapping
    public boolean deletePlace(@Argument UUID uuid) {
        return placeService.delete(uuid);
    }
}
