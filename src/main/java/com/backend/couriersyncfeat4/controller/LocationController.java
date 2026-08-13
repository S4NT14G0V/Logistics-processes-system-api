package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.entity.CustomResponseEntity;
import com.backend.couriersyncfeat4.entity.LocationEntity;
import com.backend.couriersyncfeat4.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @MutationMapping
    public LocationEntity addLocation(@Argument LocationEntity location){
        return locationService.addLocation(location);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<LocationEntity> findAllLocations() {
        return locationService.findAllLocations();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public LocationEntity findLocationById(@Argument Long id) {
        return locationService.findLocationById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @MutationMapping
    public CustomResponseEntity updateLocation(@Argument LocationEntity location){
        return locationService.updateLocation(location);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @MutationMapping
    public CustomResponseEntity deleteLocationById(@Argument Long id){
        return locationService.deleteLocationById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<LocationEntity> findAllLocationsByPackageId(@Argument Long packageId){
        return locationService.findAllLocationsByPackageEntityId(packageId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public LocationEntity findLastLocationByPackageId(@Argument Long packageId){
        return locationService.findLastLocationByPackageEntityId(packageId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<LocationEntity> findAllLocationsByUserId(@Argument Long userId){
        return locationService.findAllLocationsByUserId(userId);
    }
}
