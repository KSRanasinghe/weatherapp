package com.ksr.weatherapp.controller;

import com.ksr.weatherapp.dto.WeatherRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/test-weather")
    public WeatherRes testWeather() {
        String cityCode = "1248991"; // Colombo

        String url = "https://api.openweathermap.org/data/2.5/weather?id="
                + cityCode + "&appid=" + apiKey;

        WeatherRes response = restTemplate.getForObject(url, WeatherRes.class);

        System.out.println(response);

        return response;
    }
}
