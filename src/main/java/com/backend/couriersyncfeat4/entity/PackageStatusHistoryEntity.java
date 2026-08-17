package com.backend.couriersyncfeat4.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "package_status_history")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PackageStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity packageEntity;

    @ManyToOne
    @JoinColumn(name = "from_status_id")
    private PackageStatusEntity fromStatus;

    @ManyToOne
    @JoinColumn(name = "to_status_id", nullable = false)
    private PackageStatusEntity toStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "changed_by_user_id")
    private UserEntity changedBy;

    @Column(name = "description")
    private String description;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
