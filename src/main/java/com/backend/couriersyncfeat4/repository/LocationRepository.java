package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    @Query("""
    SELECT a
    FROM LocationEntity a
    WHERE a.packageEntity.uuid = :packageUuid
    ORDER BY a.updatedAt DESC
""")
    List<LocationEntity> findAllByPackageEntity_Uuid(@Param("packageUuid") UUID packageUuid);

    @Query("""
    SELECT a
    FROM LocationEntity a
    WHERE a.packageEntity.uuid = :packageUuid
    ORDER BY a.id ASC
""")
    List<LocationEntity> findAllByPackageEntity_UuidOrderByIdAsc(@Param("packageUuid") UUID packageUuid);

    @Query("""
    SELECT a
    FROM LocationEntity a
    ORDER BY a.updatedAt DESC
""")
    List<LocationEntity> findAllOrderByUpdatedAtDesc();

    @Query("""
    SELECT a
    FROM LocationEntity a
    WHERE a.handlerUser.id = :handlerUserId
    ORDER BY a.updatedAt DESC
""")
    List<LocationEntity> findAllByHandlerUser_Id(@Param("handlerUserId") UUID handlerUserId);
}
