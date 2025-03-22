package com.kshitijkochhar.weathersmtp.controllers;

import com.kshitijkochhar.weathersmtp.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${email.recipient}")
    private String recipient;

    @Value("${weather.location.latitude}")
    private double latitude;

    @Value("${weather.location.longitude}")
    private double longitude;

    @GetMapping("/weather")
    public String getWeatherAndSendEmail() {
        try {
            // Fetch weather data
            Map<String, Object> weatherData = weatherService.getCurrentWeather(latitude, longitude);
            
            // Format the weather message
            String message = weatherService.formatWeatherMessage(weatherData);
            
            // Send email
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(recipient);
            email.setText(message);
            mailSender.send(email);
            
            return "Weather data fetched and email sent: " + message;
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return "Error: " + e.getMessage() + " (see server logs for details)";
        }
    }
} 