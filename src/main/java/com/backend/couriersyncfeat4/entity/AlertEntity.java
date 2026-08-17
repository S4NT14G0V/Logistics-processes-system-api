package com.backend.couriersyncfeat4.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="alert")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="package_id", nullable = false)
    private PackageEntity packageEntity;

    @ManyToOne
    @JoinColumn(name="alert_type_id", nullable = false)
    private AlertTypeEntity alertTypeEntity;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
}
