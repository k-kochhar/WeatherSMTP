package com.kshitijkochhar.weathersmtp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

@Service
public class GeolocationService {

    private final RestTemplate restTemplate;
    
    @Value("${opencage.api.key}")
    private String apiKey;
    
    @Value("${opencage.api.url}")
    private String geocodingApiUrl;
    
    public GeolocationService() {
        this.restTemplate = new RestTemplate();
    }
    
    @PostConstruct
    public void init() {
        // Test the geocoding service at startup
        System.out.println("==== TESTING OPENCAGE GEOCODING SERVICE ====");
        System.out.println("API Key: " + apiKey);
        System.out.println("Geocoding API URL: " + geocodingApiUrl);
        
        Map<String, Double> testCoords = getCoordinates("New York");
        if (testCoords != null) {
            System.out.println("Geocoding test successful! New York coordinates: " + testCoords);
        } else {
            System.out.println("Geocoding test failed for 'New York'");
        }
        System.out.println("==== END TEST ====");
    }
    
    /**
     * Get coordinates for a city name using OpenCage API
     * @param city City name (can include state and country codes)
     * @return Map with lat and lon keys, or null if not found
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> getCoordinates(String city) {
        if (city == null || city.trim().isEmpty()) {
            System.out.println("City name is null or empty");
            return null;
        }
        
        try {
            // Clean and encode the input
            String cleanedCity = city.trim();
            
            // Build API URL with proper encoding
            String url = geocodingApiUrl + 
                    "?q=" + UriUtils.encodeQueryParam(cleanedCity, StandardCharsets.UTF_8) + 
                    "&key=" + apiKey + 
                    "&limit=1";
            
            System.out.println("OpenCage Geocoding API URL: " + url);
            
            // Make API call
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    if (firstResult.containsKey("geometry")) {
                        Map<String, Double> geometry = (Map<String, Double>) firstResult.get("geometry");
                        Map<String, Double> coordinates = new HashMap<>();
                        coordinates.put("lat", geometry.get("lat"));
                        coordinates.put("lon", geometry.get("lng"));
                        System.out.println("Found coordinates: " + coordinates);
                        return coordinates;
                    }
                }
            }
            
            System.out.println("No coordinates found for city: " + city);
            System.out.println("API Response: " + response);
            return null;
        } catch (Exception e) {
            System.err.println("Error getting coordinates for city: " + city + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validates and cleans city input
     * @param city User input for city
     * @return Cleaned city name or null if invalid
     */
    public String validateAndCleanCityInput(String city) {
        if (city == null || city.trim().isEmpty()) {
            return null;
        }
        
        // Remove special characters except commas, spaces, and basic punctuation
        String cleaned = city.replaceAll("[^a-zA-Z0-9\\s,.-]", "").trim();
        
        // Ensure it's not just punctuation
        if (cleaned.replaceAll("[\\s,.-]", "").isEmpty()) {
            return null;
        }
        
        return cleaned;
    }
} 