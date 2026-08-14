package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackageStatusHistoryRepository extends JpaRepository<PackageStatusHistoryEntity, Long> {

    List<PackageStatusHistoryEntity> findAllByPackageEntity_UuidOrderByChangedAtAsc(UUID packageUuid);
}
