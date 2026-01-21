package com.roadtech.service;

import com.roadtech.entity.MechanicProfile;
import com.roadtech.repository.MechanicProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final MechanicProfileRepository mechanicProfileRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public List<MechanicProfile> findNearestMechanics(BigDecimal latitude, BigDecimal longitude, int limit) {
        return mechanicProfileRepository.findNearestAvailableMechanics(latitude, longitude, limit);
    }

    public MechanicProfile findNearestMechanic(BigDecimal latitude, BigDecimal longitude) {
        List<MechanicProfile> mechanics = findNearestMechanics(latitude, longitude, 1);
        return mechanics.isEmpty() ? null : mechanics.get(0);
    }

    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public int estimateArrivalMinutes(double distanceKm) {
        // Assume average speed of 30 km/h in urban areas
        double averageSpeedKmPerHour = 30.0;
        return (int) Math.ceil((distanceKm / averageSpeedKmPerHour) * 60);
    }
}
