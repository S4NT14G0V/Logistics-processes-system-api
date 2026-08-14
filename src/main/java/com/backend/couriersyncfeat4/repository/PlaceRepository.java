package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.entity.PlaceEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
    Optional<PlaceEntity> findByUuid(UUID uuid);
}