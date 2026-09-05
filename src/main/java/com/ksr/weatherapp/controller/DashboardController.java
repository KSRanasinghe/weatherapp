package com.ksr.weatherapp.controller;

import com.ksr.weatherapp.dto.CityComfortResult;
import com.ksr.weatherapp.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final WeatherService weatherService;

//    @GetMapping("/test-weather")
//    public WeatherRes testWeather() {
//        String cityCode = "1248991"; // Colombo
//
//        String url = "https://api.openweathermap.org/data/2.5/weather?id="
//                + cityCode + "&appid=" + apiKey;
//
//        WeatherRes response = restTemplate.getForObject(url, WeatherRes.class);
//
//        System.out.println(response);
//
//        return response;
//    }

    @GetMapping("/weather-all")
    public List<CityComfortResult> getWeather() {
        return weatherService.getAllCityComfort();
    }

    @GetMapping("/cache-status")
    public Map<String, String> getCacheStatus() {
        return weatherService.getCacheStatus();
    }
}
