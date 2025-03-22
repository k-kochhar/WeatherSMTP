package com.kshitijkochhar.weathersmtp.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
public class GeocodeTestController {

    @Value("${opencage.api.key}")
    private String apiKey;
    
    @Value("${opencage.api.url}")
    private String geocodingApiUrl;

    @GetMapping("/test-geocode")
    public String testGeocode(@RequestParam String city) {
        try {
            // Clean and encode the input
            String cleanedCity = city.trim();
            
            // Build API URL with proper encoding
            String urlString = geocodingApiUrl + 
                    "?q=" + UriUtils.encodeQueryParam(cleanedCity, StandardCharsets.UTF_8) + 
                    "&key=" + apiKey + 
                    "&limit=1";
            
            StringBuilder result = new StringBuilder();
            result.append("Testing OpenCage geocoding for: ").append(city).append("\n");
            result.append("URL: ").append(urlString).append("\n\n");
            
            URI uri = URI.create(urlString);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            result.append("Response Code: ").append(conn.getResponseCode()).append("\n\n");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                result.append("Response Body: ").append(response.toString());
            }
            
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error testing geocode: " + e.getMessage();
        }
    }
} 