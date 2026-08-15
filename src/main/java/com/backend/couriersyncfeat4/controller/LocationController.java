package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.LocationAddInput;
import com.backend.couriersyncfeat4.dto.output.LocationResponse;
import com.backend.couriersyncfeat4.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("hasAuthority('location:create:all')")
    @MutationMapping
    public LocationResponse addLocation(@Argument UUID packageId, @Argument("input") @Valid LocationAddInput input) {
        return locationService.addLocation(packageId, input);
    }

    @PreAuthorize("hasAuthority('location:read:all')")
    @QueryMapping
    public List<LocationResponse> findAllLocations() {
        return locationService.findAllLocations();
    }

    @PreAuthorize("hasAuthority('location:read:all')")
    @QueryMapping
    public LocationResponse findLocationById(@Argument Long id) {
        return locationService.findLocationById(id);
    }

    @PreAuthorize("hasAnyAuthority('location:read:all','location:read:own')")
    @QueryMapping
    public List<LocationResponse> findAllLocationsByPackageId(@Argument UUID packageId) {
        return locationService.findAllLocationsByPackageId(packageId);
    }

    @PreAuthorize("hasAnyAuthority('location:read:all','location:read:own')")
    @QueryMapping
    public List<LocationResponse> findLocationsByTrackingCode(@Argument String trackingCode) {
        return locationService.findLocationsByTrackingCode(trackingCode);
    }

    @PreAuthorize("hasAnyAuthority('location:read:all','location:read:own')")
    @QueryMapping
    public LocationResponse findLastLocationByPackageId(@Argument UUID packageId) {
        return locationService.findLastLocationByPackageId(packageId);
    }

    @PreAuthorize("hasAnyAuthority('location:read:all','location:read:own')")
    @QueryMapping
    public List<LocationResponse> findAllLocationsByUserId(@Argument UUID userId) {
        return locationService.findAllLocationsByUserId(userId);
    }

    @PreAuthorize("hasAuthority('location:update:all')")
    @MutationMapping
    public LocationResponse updateLocation(@Argument Long id, @Argument("input") @Valid LocationAddInput input) {
        return locationService.updateLocation(id, input);
    }

    @PreAuthorize("hasAuthority('location:delete:all')")
    @MutationMapping
    public boolean deleteLocation(@Argument Long id) {
        return locationService.deleteLocation(id);
    }
}
