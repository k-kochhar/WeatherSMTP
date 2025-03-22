package com.kshitijkochhar.weathersmtp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    
    @Value("${openweather.api.key}")
    private String apiKey;
    
    @Value("${openweather.api.url}")
    private String apiUrl;
    
    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }
    
    public Map<String, Object> getCurrentWeather(double lat, double lon) {
        // For OneCall API 3.0, we need to specify exclude to make sure we get daily data
        String url = String.format("%s?lat=%f&lon=%f&exclude=minutely,hourly&appid=%s&units=imperial", apiUrl, lat, lon, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }
    
    public String formatWeatherMessage(Map<String, Object> weatherData) {
        try {
            // Get the daily forecast data
            List<Map<String, Object>> dailyData = (List<Map<String, Object>>) weatherData.get("daily");
            
            // Get the first day's forecast (today)
            Map<String, Object> todayForecast = dailyData.get(0);
            
            // Get the temperature data for today
            Map<String, Object> tempData = (Map<String, Object>) todayForecast.get("temp");
            
            // Extract min and max temperatures
            double maxTemp = (double) tempData.get("max");
            double minTemp = (double) tempData.get("min");
            
            // Create the weather message
            return String.format(
                "It is a great morning in Seattle! Today we have a high of %.1f and a low of %.1f! Today is going to be a good day!",
                maxTemp,
                minTemp
            );
        } catch (Exception e) {
            // If there's an error parsing the response, log it and return a generic message
            System.err.println("Error parsing weather data: " + e.getMessage());
            return "Weather data is currently unavailable. Please try again later.";
        }
    }
} 