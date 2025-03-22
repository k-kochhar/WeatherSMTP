package com.kshitijkochhar.weathersmtp.config;

import com.kshitijkochhar.weathersmtp.models.User;
import com.kshitijkochhar.weathersmtp.repositories.UserRepository;
import com.kshitijkochhar.weathersmtp.services.GeolocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GeolocationService geolocationService;

    @Override
    public void run(String... args) throws Exception {
        // Check if we have any users already
        if (userRepository.count() == 0) {
            // Create default user (your information)
            User defaultUser = new User();
            defaultUser.setName("Kshitij");
            defaultUser.setPhoneNumber("6105630579");
            defaultUser.setEmailProvider("vtext.com");
            defaultUser.setCity("Seattle, WA, USA");
            
            // Get coordinates from the geolocation service
            Map<String, Double> coordinates = geolocationService.getCoordinates("Seattle, WA, USA");
            if (coordinates != null) {
                defaultUser.setLatitude(coordinates.get("lat"));
                defaultUser.setLongitude(coordinates.get("lon"));
            } else {
                // Fallback to hardcoded coordinates if geocoding fails
                defaultUser.setLatitude(47.6061);
                defaultUser.setLongitude(-122.3328);
            }
            
            defaultUser.setActive(true);
            
            userRepository.save(defaultUser);
            
            System.out.println("Initial user data loaded successfully.");
        }
    }
} 