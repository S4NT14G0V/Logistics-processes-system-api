package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.PackageCountByUserDTO;
import com.backend.couriersyncfeat4.entity.*;
import com.backend.couriersyncfeat4.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PackageService {

    private final PackageRepository packageRepository;
    private final UserService userService;
    private final PackageStatusService packageStatusService;

    @Autowired
    public PackageService(PackageRepository packageRepository, UserService userService, PackageStatusService packageStatusService) {
        this.packageRepository = packageRepository;
        this.userService = userService;
        this.packageStatusService = packageStatusService;
    }

    // TODO: make validations in each function
    public PackageEntity addPackage(PackageEntity packageEntity) {

        if (packageEntity == null || packageEntity.getOrigin() == null || packageEntity.getDestination() == null
        || packageEntity.getOwnerUser() == null || packageEntity.getStatus() == null) {
            throw new RuntimeException("Some value is null");
        }

        UserEntity userEntity = userService.findUserById(packageEntity.getOwnerUser().getId());
        PackageStatusEntity packageStatusEntity = packageStatusService.findById(packageEntity.getStatus().getId());

        PackageEntity packageEntityAux = new PackageEntity();
        packageEntityAux.setOwnerUser(userEntity);
        packageEntityAux.setStatus(packageStatusEntity);
        packageEntityAux.setDescription(packageEntity.getDescription());
        packageEntityAux.setOrigin(packageEntity.getOrigin());
        packageEntityAux.setDestination(packageEntity.getDestination());
        packageEntityAux.setRegisteredAt(LocalDateTime.now());

        packageRepository.save(packageEntityAux);
        return packageEntityAux;
    }

    public List<PackageEntity> findAllPackages() {
        return packageRepository.findAll();
    }

    public PackageEntity findPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
    }

    public PackageEntity findPackageByTrackingCode(String trackingCode) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            throw new RuntimeException("Tracking code is null or empty");
        }
        UUID trackingCodeUUID = UUID.fromString(trackingCode);
        return packageRepository.findByTrackingCode(trackingCodeUUID)
                .orElseThrow(() -> new RuntimeException("Package not found"));
    }

    public CustomResponseEntity updatePackage(PackageEntity packageEntity) {
        if (!packageRepository.existsById(packageEntity.getId())) {
            return new CustomResponseEntity(false, "Package not found for update");
        }
        PackageEntity oldPackageEntity = findPackageById(packageEntity.getId());

        UserEntity userEntity = packageEntity.getOwnerUser() != null ? packageEntity.getOwnerUser() : oldPackageEntity.getOwnerUser();

        PackageStatusEntity packageStatusEntity = packageEntity.getStatus() != null ? packageEntity.getStatus() : oldPackageEntity.getStatus();

        String description = packageEntity.getDescription() != null ? packageEntity.getDescription() : oldPackageEntity.getDescription();

        String origin = packageEntity.getDestination() != null ? packageEntity.getOrigin() : oldPackageEntity.getOrigin();

        String destination = packageEntity.getDestination() != null ? packageEntity.getDestination() : oldPackageEntity.getDestination();

        oldPackageEntity.setOwnerUser(userEntity);
        oldPackageEntity.setStatus(packageStatusEntity);
        oldPackageEntity.setDescription(description);
        oldPackageEntity.setOrigin(origin);
        oldPackageEntity.setDestination(destination);

        packageRepository.save(oldPackageEntity);
        return new CustomResponseEntity(true, "Package successfully updated");
    }

    public CustomResponseEntity deletePackageById(Long id) {
        if (!packageRepository.existsById(id)) {
            return new CustomResponseEntity(false, "Package with id " + id + " does not exist");
        }
        packageRepository.deleteById(id);
        return new CustomResponseEntity(true, "Package with id " + id + " successfully deleted");
    }

    public List<PackageEntity> findPackagesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return packageRepository.findByRegisteredAtBetween(startDate, endDate);
        } else if (startDate != null) {
            return packageRepository.findByRegisteredAtAfter(startDate);
        } else if (endDate != null) {
            return packageRepository.findByRegisteredAtBefore(endDate);
        }
        return Collections.emptyList();
    }

    public PackageCountByUserDTO findPackageCountByUserId(Long userId){
        PackageCountByUserDTO packageCountByUserDTO = packageRepository.findCountByUserId(userId);
        if (packageCountByUserDTO != null && packageCountByUserDTO.getUserId() != null && packageCountByUserDTO.getPackageCount() != null){
            return packageCountByUserDTO;
        }
        PackageCountByUserDTO packageCountByUserNew = new PackageCountByUserDTO();
        packageCountByUserNew.setUserId(userId);
        packageCountByUserNew.setPackageCount((long) 0);
        return packageCountByUserNew;
    }

    public List<PackageEntity> findPackagesByStatusIn(List<Integer> packageStatusesId) {
        if (packageStatusesId.isEmpty()) {
            throw new IllegalArgumentException("La lista de estados no puede ser nula o vacía");
        }
        List<PackageStatusEntity> packageStatusEntities = new ArrayList<>();
        for(Integer statusId : packageStatusesId){
            packageStatusEntities.add(packageStatusService.findById(statusId));
        }
        return packageRepository.findByStatusIn(packageStatusEntities);
    }

    public List<PackageCountByUserDTO> findCountByAllUsers(){
        return packageRepository.findCountByAllUsers();
    }

    public List<PackageEntity> findAllPackagesByUserId(Long userId){
        return packageRepository.findAllByOwnerUser_Id(userId);
    }

    public List<PackageEntity> findAllPackagesByUbication(String origin, String destination){
        if((origin == null || origin.isEmpty()) && (destination == null || destination.isEmpty())){
            return Collections.emptyList();
        }

        if ((origin == null || origin.trim().isEmpty()) && (destination != null)){
            return packageRepository.findAllByDestination(destination);
        }
        else if((destination == null || destination.trim().isEmpty())){
            return packageRepository.findAllByOrigin(origin);
        }
        return packageRepository.findAllByOriginAndDestination(origin, destination);
    }

}
