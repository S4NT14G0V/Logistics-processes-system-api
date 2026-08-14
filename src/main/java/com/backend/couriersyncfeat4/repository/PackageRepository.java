package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.dto.output.PackageCountProjection;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Optional<PackageEntity> findByTrackingCode(String trackingCode);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findByRegisteredAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findByRegisteredAtAfter(LocalDateTime startDate);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findByRegisteredAtBefore(LocalDateTime endDate);

    @Query("SELECT p.ownerUser.id AS userId, COUNT(p) AS packageCount " +
            "FROM PackageEntity p WHERE p.ownerUser.id = :userId GROUP BY p.ownerUser.id")
    PackageCountProjection findCountByUserId(@Param("userId") UUID userId);

    @Query("SELECT p.ownerUser.id AS userId, COUNT(p) AS packageCount " +
            "FROM PackageEntity p GROUP BY p.ownerUser.id")
    List<PackageCountProjection> findCountByAllUsers();

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findByStatusIn(List<PackageStatusEntity> packageStatusEntities);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAllByOwnerUser_Id(UUID userId);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAllByOwnerUser_Email(String email);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAllByDestination_Uuid(UUID destinationUuid);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAllByOrigin_Uuid(UUID originUuid);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAllByOrigin_UuidAndDestination_Uuid(UUID originUuid, UUID destinationUuid);

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    List<PackageEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByOwnerUser_Email(String email, Pageable pageable);
}
