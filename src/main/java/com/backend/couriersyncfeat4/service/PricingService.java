package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.PlaceEntity;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final double BASE_PRICE = 5000.0;
    private static final double PRICE_PER_KG = 1000.0;
    private static final double PRICE_PER_KM = 500.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculatePrice(Double weightKg, Double distanceKm) {
        double weight = weightKg == null ? 0.0 : weightKg;
        double distance = distanceKm == null ? 0.0 : distanceKm;
        return BASE_PRICE + (weight * PRICE_PER_KG) + (distance * PRICE_PER_KM);
    }

    public double distanceKm(PlaceEntity origin, PlaceEntity destination) {
        if (origin == null || destination == null
                || origin.getLatitude() == null || origin.getLongitude() == null
                || destination.getLatitude() == null || destination.getLongitude() == null) {
            return 0.0;
        }

        double lat1 = Math.toRadians(origin.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon2 = Math.toRadians(destination.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
