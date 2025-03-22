package com.kshitijkochhar.weathersmtp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }
    
    @SuppressWarnings("unchecked")
    public String generateWeatherMessage(Map<String, Object> weatherData) {
        try {
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> contents = new HashMap<>();
            contents.put("role", "user");
            
            String prompt = "Based on this weather data: " + weatherData.toString() + 
                "Generate a brief, fun message about today's weather. " +
                "Include a high and low temperature and a suggestion for the day in terms of clothing or accessories" +
                "Keep it cheerful and under 160 characters to fit in a text message. " +
                "Do not include emojis or non ASCII characters. " +

                "Sample Response: Good Morning Seattle! Today we have a low of [low] and a high of [high]. " +
                "Make sure to pack an umbrella today! :D";
            contents.put("parts", List.of(Map.of("text", prompt)));

            requestBody.put("contents", List.of(contents));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(geminiUrl, request, Map.class);
            
            // Extract the generated text from the response
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (!parts.isEmpty() && parts.get(0).containsKey("text")) {
                                return (String) parts.get(0).get("text");
                            }
                        }
                    }
                }
            }
            
            // Fallback if we can't parse the response
            throw new RuntimeException("Failed to parse Gemini API response");
            
        } catch (Exception e) {
            System.err.println("Error generating message with Gemini: " + e.getMessage());
            
            // Fallback to using the default message format
            try {
                // Extract high and low temperatures
                List<Map<String, Object>> dailyData = (List<Map<String, Object>>) weatherData.get("daily");
                Map<String, Object> todayForecast = dailyData.get(0);
                Map<String, Object> tempData = (Map<String, Object>) todayForecast.get("temp");
                double maxTemp = (double) tempData.get("max");
                double minTemp = (double) tempData.get("min");
                
                return String.format(
                    "It is a great morning in Seattle! Today we have a high of %.1f and a low of %.1f! Today is going to be a good day!",
                    maxTemp, minTemp
                );
            } catch (Exception ex) {
                return "Weather update unavailable. Have a wonderful day anyway!";
            }
        }
    }
} 