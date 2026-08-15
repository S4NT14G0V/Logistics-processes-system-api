package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.config.Permission;
import com.backend.couriersyncfeat4.dto.input.LocationAddInput;
import com.backend.couriersyncfeat4.dto.output.LocationResponse;
import com.backend.couriersyncfeat4.entity.LocationEntity;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.LocationMapper;
import com.backend.couriersyncfeat4.repository.LocationRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserService userService;
    private final PackageService packageService;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, UserService userService,
                           PackageService packageService, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.userService = userService;
        this.packageService = packageService;
        this.locationMapper = locationMapper;
    }

    public LocationResponse addLocation(UUID packageId, LocationAddInput input) {
        PackageEntity packageEntity = packageService.getPackageById(packageId);
        UserEntity handler = userService.getCurrentUser();

        LocationEntity location = new LocationEntity();
        location.setPackageEntity(packageEntity);
        location.setHandlerUser(handler);
        location.setLatitude(input.latitude());
        location.setLongitude(input.longitude());
        location.setAddress(input.address());
        location.setUpdatedAt(LocalDateTime.now());

        return locationMapper.toResponse(locationRepository.save(location));
    }

    public List<LocationResponse> findAllLocations() {
        return locationRepository.findAllOrderByUpdatedAtDesc().stream()
                .map(locationMapper::toResponse).toList();
    }

    public LocationResponse findLocationById(Long id) {
        return locationMapper.toResponse(getById(id));
    }

    public LocationEntity getById(Long id) {
        if (id == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "Location id is required");
        }
        return locationRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.LOCATION_NOT_FOUND));
    }

    public LocationResponse updateLocation(Long id, LocationAddInput input) {
        LocationEntity location = getById(id);
        location.setLatitude(input.latitude());
        location.setLongitude(input.longitude());
        location.setAddress(input.address());
        location.setUpdatedAt(LocalDateTime.now());
        return locationMapper.toResponse(locationRepository.save(location));
    }

    public boolean deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ApplicationException(ErrorCodes.LOCATION_NOT_FOUND, "Location not found");
        }
        locationRepository.deleteById(id);
        return true;
    }

    public List<LocationResponse> findAllLocationsByPackageId(UUID packageId) {
        assertCanReadPackage(packageService.getPackageById(packageId));
        return locationRepository.findAllByPackageEntity_Uuid(packageId).stream()
                .map(locationMapper::toResponse).toList();
    }

    public List<LocationResponse> findLocationsByTrackingCode(String trackingCode) {
        PackageEntity packageEntity = packageService.getByTrackingCode(trackingCode);
        assertCanReadPackage(packageEntity);
        return locationRepository.findAllByPackageEntity_Uuid(packageEntity.getUuid()).stream()
                .map(locationMapper::toResponse).toList();
    }

    public LocationResponse findLastLocationByPackageId(UUID packageId) {
        assertCanReadPackage(packageService.getPackageById(packageId));
        List<LocationEntity> locations = locationRepository.findAllByPackageEntity_UuidOrderByIdAsc(packageId);
        if (locations.isEmpty()) {
            throw new ApplicationException(ErrorCodes.LOCATION_NOT_FOUND, "No locations found for package");
        }
        return locationMapper.toResponse(locations.get(locations.size() - 1));
    }

    public List<LocationResponse> findAllLocationsByUserId(UUID userId) {
        if (!SecurityUtils.hasPermission(Permission.LOCATION_READ_ALL)) {
            UserEntity current = userService.getCurrentUser();
            if (current == null || !current.getId().equals(userId)) {
                throw new ApplicationException(ErrorCodes.FORBIDDEN);
            }
        }
        return locationRepository.findAllByHandlerUser_Id(userId).stream()
                .map(locationMapper::toResponse).toList();
    }

    private void assertCanReadPackage(PackageEntity packageEntity) {
        if (SecurityUtils.hasPermission(Permission.LOCATION_READ_ALL)) {
            return;
        }
        UserEntity current = userService.getCurrentUser();
        if (packageEntity.getOwnerUser() == null
                || !packageEntity.getOwnerUser().getEmail().equals(current.getEmail())) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN);
        }
    }
}
