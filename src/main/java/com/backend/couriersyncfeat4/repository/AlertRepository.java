package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    @Query("""
    SELECT a
    FROM AlertEntity a
    ORDER BY a.registeredAt DESC
""")
    List<AlertEntity> findAllOrderByRegisteredAtDesc();

    @Query("""
    SELECT a
    FROM AlertEntity a
    WHERE a.user.id = :userId
    ORDER BY a.registeredAt DESC
""")
    List<AlertEntity> findAllByUserId(@Param("userId") UUID userId);

}
