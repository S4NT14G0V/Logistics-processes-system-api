package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.dto.output.PackageCountProjection;
import com.backend.couriersyncfeat4.dto.output.StatusCountProjection;
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

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination",
            "statusHistory", "statusHistory.fromStatus", "statusHistory.toStatus", "statusHistory.changedBy"})
    Optional<PackageEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination",
            "statusHistory", "statusHistory.fromStatus", "statusHistory.toStatus", "statusHistory.changedBy"})
    Optional<PackageEntity> findByTrackingCode(String trackingCode);

    boolean existsByTrackingCode(String trackingCode);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findByRegisteredAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findByRegisteredAtAfter(LocalDateTime startDate, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findByRegisteredAtBefore(LocalDateTime endDate, Pageable pageable);

    @Query("SELECT p.ownerUser.id AS userId, COUNT(p) AS packageCount " +
            "FROM PackageEntity p WHERE p.ownerUser.id = :userId GROUP BY p.ownerUser.id")
    PackageCountProjection findCountByUserId(@Param("userId") UUID userId);

    @Query("SELECT p.ownerUser.id AS userId, COUNT(p) AS packageCount " +
            "FROM PackageEntity p GROUP BY p.ownerUser.id")
    List<PackageCountProjection> findCountByAllUsers();

    @Query("SELECT p.status.code AS statusCode, COUNT(p) AS count " +
            "FROM PackageEntity p GROUP BY p.status.code")
    List<StatusCountProjection> findCountByStatus();

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findByStatusIn(List<PackageStatusEntity> packageStatusEntities, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByOwnerUser_Id(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByDestination_Uuid(UUID destinationUuid, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByOrigin_Uuid(UUID originUuid, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByOrigin_UuidAndDestination_Uuid(UUID originUuid, UUID destinationUuid, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser", "status", "origin", "destination"})
    Page<PackageEntity> findAllByOwnerUser_Email(String email, Pageable pageable);
}
