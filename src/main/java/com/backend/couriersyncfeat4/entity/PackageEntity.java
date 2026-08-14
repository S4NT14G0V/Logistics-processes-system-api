package com.backend.couriersyncfeat4.entity;

import com.backend.couriersyncfeat4.enums.PackageStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "package")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity ownerUser;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private PackageStatusEntity status;

    @Column(name = "tracking_code", updatable = false, nullable = false, unique = true)
    private String trackingCode;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime updatedAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationEntity> locationEntities;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlertEntity> alertEntities;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageStatusHistoryEntity> statusHistory;

    @ManyToOne
    @JoinColumn(name = "origin_place_id", nullable = false)
    private PlaceEntity origin;

    @ManyToOne
    @JoinColumn(name = "destination_place_id", nullable = false)
    private PlaceEntity destination;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (registeredAt == null) {
            registeredAt = now;
        }
        updatedAt = now;
        if (trackingCode == null) {
            trackingCode = generateTrackingCode();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateFrom(String newDescription, PlaceEntity newOrigin, PlaceEntity newDestination) {
        if (newDescription != null) {
            this.description = newDescription;
        }
        if (newOrigin != null) {
            this.origin = newOrigin;
        }
        if (newDestination != null) {
            this.destination = newDestination;
        }
    }

    public boolean isCreated() {
        return hasStatus(PackageStatusEnum.CREATED);
    }

    public boolean isInTransit() {
        return hasStatus(PackageStatusEnum.IN_TRANSIT);
    }

    public boolean isDelivered() {
        return hasStatus(PackageStatusEnum.DELIVERED);
    }

    public boolean isCancelled() {
        return hasStatus(PackageStatusEnum.CANCELLED);
    }

    private boolean hasStatus(PackageStatusEnum expected) {
        return status != null && expected.getCode().equals(status.getCode());
    }

    private static String generateTrackingCode() {
        return "AMAZN-" + randomSegment(5) + "-" + randomSegment(6);
    }

    private static String randomSegment(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * alphabet.length());
            builder.append(alphabet.charAt(index));
        }
        return builder.toString();
    }
}
