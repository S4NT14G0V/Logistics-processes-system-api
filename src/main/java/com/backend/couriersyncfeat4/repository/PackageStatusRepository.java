package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageStatusRepository extends JpaRepository<PackageStatusEntity, Integer> {

    Optional<PackageStatusEntity> findByCode(String code);

    List<PackageStatusEntity> findByCodeIn(List<String> codes);
}
