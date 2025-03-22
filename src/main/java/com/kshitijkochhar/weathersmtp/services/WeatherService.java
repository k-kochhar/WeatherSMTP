package com.kshitijkochhar.weathersmtp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCurrentWeather(double lat, double lon) {
        // For OneCall API 3.0, we need to specify exclude to make sure we get daily data
        String url = String.format("%s?lat=%f&lon=%f&exclude=minutely,hourly&appid=%s&units=imperial", apiUrl, lat, lon, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }
} 