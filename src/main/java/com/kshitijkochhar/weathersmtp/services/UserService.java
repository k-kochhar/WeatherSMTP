package com.kshitijkochhar.weathersmtp.services;

import com.kshitijkochhar.weathersmtp.models.User;
import com.kshitijkochhar.weathersmtp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GeolocationService geolocationService;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByActive(true);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User createUser(User user) {
        System.out.println("Creating user with data: " + user.getName() + ", city: " + user.getCity());
        
        // Clean and validate phone number (remove non-numeric characters)
        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().replaceAll("[^0-9]", ""));
        }
        
        // If city is provided but coordinates are not, get coordinates from city
        if (user.getCity() != null && !user.getCity().trim().isEmpty() && 
                (user.getLatitude() == null || user.getLongitude() == null)) {
            System.out.println("User has city but no coordinates. Getting coordinates for: " + user.getCity());
            String cleanedCity = geolocationService.validateAndCleanCityInput(user.getCity());
            System.out.println("Cleaned city: " + cleanedCity);
            
            if (cleanedCity != null) {
                user.setCity(cleanedCity);
                Map<String, Double> coordinates = geolocationService.getCoordinates(cleanedCity);
                System.out.println("Geocoding result: " + coordinates);
                
                if (coordinates != null) {
                    user.setLatitude(coordinates.get("lat"));
                    user.setLongitude(coordinates.get("lon"));
                    System.out.println("Set coordinates: " + user.getLatitude() + ", " + user.getLongitude());
                } else {
                    System.out.println("Failed to get coordinates for: " + cleanedCity);
                }
            } else {
                System.out.println("City name validation failed");
            }
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userDetails.getName() != null) {
                        existingUser.setName(userDetails.getName());
                    }
                    
                    // Clean and validate phone number if provided
                    if (userDetails.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(userDetails.getPhoneNumber().replaceAll("[^0-9]", ""));
                    }
                    
                    if (userDetails.getEmailProvider() != null) {
                        existingUser.setEmailProvider(userDetails.getEmailProvider());
                    }
                    
                    // Handle city and coordinates
                    if (userDetails.getCity() != null && !userDetails.getCity().trim().isEmpty()) {
                        String cleanedCity = geolocationService.validateAndCleanCityInput(userDetails.getCity());
                        if (cleanedCity != null) {
                            existingUser.setCity(cleanedCity);
                            // Update coordinates based on city if lat/lon aren't explicitly provided
                            if ((userDetails.getLatitude() == null || userDetails.getLongitude() == null)) {
                                Map<String, Double> coordinates = geolocationService.getCoordinates(cleanedCity);
                                if (coordinates != null) {
                                    existingUser.setLatitude(coordinates.get("lat"));
                                    existingUser.setLongitude(coordinates.get("lon"));
                                }
                            }
                        }
                    }
                    
                    // If lat/lon are explicitly provided, use those values
                    if (userDetails.getLatitude() != null) {
                        existingUser.setLatitude(userDetails.getLatitude());
                    }
                    if (userDetails.getLongitude() != null) {
                        existingUser.setLongitude(userDetails.getLongitude());
                    }
                    
                    if (userDetails.getActive() != null) {
                        existingUser.setActive(userDetails.getActive());
                    }
                    return userRepository.save(existingUser);
                });
    }
    
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                }).orElse(false);
    }
    
    // Alternative to hard delete - just deactivate the user
    public boolean deactivateUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(false);
                    userRepository.save(user);
                    return true;
                }).orElse(false);
    }
} 