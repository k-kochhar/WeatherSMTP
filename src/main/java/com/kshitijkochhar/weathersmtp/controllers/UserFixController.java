package com.kshitijkochhar.weathersmtp.controllers;

import com.kshitijkochhar.weathersmtp.models.User;
import com.kshitijkochhar.weathersmtp.services.GeolocationService;
import com.kshitijkochhar.weathersmtp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class UserFixController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeolocationService geolocationService;

    @GetMapping("/fix-coordinates")
    public ResponseEntity<Map<String, Object>> fixCoordinates() {
        List<User> users = userRepository.findAll();
        int updatedCount = 0;
        Map<String, Object> result = new HashMap<>();
        
        for (User user : users) {
            if (user.getCity() != null && !user.getCity().isEmpty() && 
                (user.getLatitude() == null || user.getLongitude() == null)) {
                
                String cleanedCity = geolocationService.validateAndCleanCityInput(user.getCity());
                if (cleanedCity != null) {
                    Map<String, Double> coordinates = geolocationService.getCoordinates(cleanedCity);
                    if (coordinates != null) {
                        user.setLatitude(coordinates.get("lat"));
                        user.setLongitude(coordinates.get("lon"));
                        userRepository.save(user);
                        updatedCount++;
                    }
                }
            }
        }
        
        result.put("success", true);
        result.put("message", "Coordinates fixed for " + updatedCount + " users");
        result.put("updatedCount", updatedCount);
        
        return ResponseEntity.ok(result);
    }
} 