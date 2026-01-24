package com.roadtech.service;

import com.roadtech.entity.MechanicProfile;
import com.roadtech.repository.MechanicProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service responsible for location-based operations related to mechanics.
 * Handles finding nearest mechanics, distance calculation, and ETA estimation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    /**
     * Repository used to fetch mechanic profile data from the database.
     */
    private final MechanicProfileRepository mechanicProfileRepository;

    /**
     * Earth's radius in kilometers.
     * Used for distance calculation using the Haversine formula.
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Finds a list of nearest available mechanics based on given latitude and longitude.
     *
     * @param latitude  latitude of the user location
     * @param longitude longitude of the user location
     * @param limit     maximum number of mechanics to return
     * @return list of nearest available mechanic profiles
     */
    public List<MechanicProfile> findNearestMechanics(BigDecimal latitude, BigDecimal longitude, int limit) {
        return mechanicProfileRepository.findNearestAvailableMechanics(latitude, longitude, limit);
    }

    /**
     * Finds the single nearest available mechanic based on given latitude and longitude.
     *
     * @param latitude  latitude of the user location
     * @param longitude longitude of the user location
     * @return nearest mechanic profile or null if none found
     */
    public MechanicProfile findNearestMechanic(BigDecimal latitude, BigDecimal longitude) {
        List<MechanicProfile> mechanics = findNearestMechanics(latitude, longitude, 1);
        return mechanics.isEmpty() ? null : mechanics.get(0);
    }

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     *
     * @param lat1 latitude of first location
     * @param lon1 longitude of first location
     * @param lat2 latitude of second location
     * @param lon2 longitude of second location
     * @return distance between the two points in kilometers
     */
    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {

        // Convert latitude values from degrees to radians
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());

        // Difference in coordinates (converted to radians)
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        // Haversine formula implementation
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Final distance in kilometers
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimates arrival time in minutes based on distance.
     * Assumes an average urban travel speed.
     *
     * @param distanceKm distance in kilometers
     * @return estimated arrival time in minutes
     */
    public int estimateArrivalMinutes(double distanceKm) {

        // Assume average speed of 30 km/h in urban areas
        double averageSpeedKmPerHour = 30.0;

        // Convert travel time from hours to minutes and round up
        return (int) Math.ceil((distanceKm / averageSpeedKmPerHour) * 60);
    }
}
