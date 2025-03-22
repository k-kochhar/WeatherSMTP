package com.kshitijkochhar.weathersmtp.controllers;

import com.kshitijkochhar.weathersmtp.models.User;
import com.kshitijkochhar.weathersmtp.services.GeminiService;
import com.kshitijkochhar.weathersmtp.services.UserService;
import com.kshitijkochhar.weathersmtp.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class WeatherController {

    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserService userService;

    @GetMapping("/weather")
    public String sendWeatherToAllUsers() {
        List<User> activeUsers = userService.getActiveUsers();
        AtomicInteger successCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();
        
        activeUsers.forEach(user -> {
            try {
                // Get email address by combining phone number and provider
                String emailAddress = user.getPhoneNumber() + "@" + user.getEmailProvider();
                
                // Fetch weather data for user's location
                Map<String, Object> weatherData = weatherService.getCurrentWeather(
                        user.getLatitude(), user.getLongitude());
                
                // Generate a personalized message
                String message = geminiService.generateWeatherMessage(weatherData);
                
                // Send email
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(emailAddress);
                email.setText(message);
                mailSender.send(email);
                
                successCount.incrementAndGet();
            } catch (Exception e) {
                errors.add("Error sending weather to user " + user.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        return String.format("Weather updates sent to %d/%d users. %s", 
                successCount.get(), activeUsers.size(),
                errors.isEmpty() ? "" : "Errors: " + String.join(", ", errors));
    }
    
    @GetMapping("/weather/{userId}")
    public String sendWeatherToSpecificUser(@PathVariable Long userId) {
        Optional<User> userOpt = userService.getUserById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                // Get email address by combining phone number and provider
                String emailAddress = user.getPhoneNumber() + "@" + user.getEmailProvider();
                
                // Fetch weather data for user's location
                Map<String, Object> weatherData = weatherService.getCurrentWeather(
                        user.getLatitude(), user.getLongitude());
                
                // Generate a personalized message
                String message = geminiService.generateWeatherMessage(weatherData);
                
                // Send email
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(emailAddress);
                email.setText(message);
                mailSender.send(email);
                
                return "Weather update sent successfully to user: " + user.getName();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error sending weather update: " + e.getMessage();
            }
        } else {
            return "User not found with ID: " + userId;
        }
    }
} 