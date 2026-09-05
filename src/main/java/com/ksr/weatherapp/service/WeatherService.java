package com.ksr.weatherapp.service;

import com.ksr.weatherapp.dto.CityComfortResult;
import com.ksr.weatherapp.dto.external.WeatherRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private final List<String> citycodes = List.of(
            "1248991", // Colombo
            "1850147", // Tokyo
            "2644210", // Liverpool
            "2988507", // Paris
            "2147714", // Sydney
            "4930956", // Boston
            "1796236", // Shanghai
            "3143244", // Oslo
            "2643743", // London
            "5128581"  // New York
    );

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<CityComfortResult> getAllCityComfort() {
        List<CityComfortResult> results = new ArrayList<>();

        for (String cityCode : citycodes) {
            String url = "https://api.openweathermap.org/data/2.5/weather?id="
                    + cityCode + "&appid=" + apiKey + "&units=metric";

            WeatherRes response = restTemplate.getForObject(url, WeatherRes.class);

            CityComfortResult result = new CityComfortResult();
            result.setCityName(response.getName());
            result.setDescription(response.getWeather().getFirst().getDescription());
            result.setTemp(response.getMain().getTemp());
            result.setComfortScore(calculateComfortScore(response));

            results.add(result);
        }

        return results;
    }

    private double calculateComfortScore(WeatherRes response) {
        double temp = response.getMain().getTemp();
        double humidity = response.getMain().getHumidity();
        double wind = response.getWind().getSpeed();

        //considering real world scenarios, ideal temp 20 - 25 Celsius & humidity 50% & wind 2-3 (2.5) m/s
        double idealTemp = 22.5, idealHumidity = 50, idealWind = 2.5;

        //considering real world scenarios, worst temp 30-33 Celsius & humidity 50% & wind 15-20 (17.5) m/s
        double tempPenalty = 3, humidityPenalty = 2, windPenalty = 6;

        double tempScore = 100 - Math.abs(temp - idealTemp) * tempPenalty;
        double humidityScore = 100 - Math.abs(humidity - idealHumidity) * humidityPenalty;
        double windScore = 100 - Math.abs(wind - idealWind) * windPenalty;

        double comfortScore = (tempScore * 0.5) + (humidityScore * 0.3) + (windScore * 0.2);
        comfortScore = Math.clamp(comfortScore, 0, 100);

        return Math.round(comfortScore * 100.0) / 100.0;
    }
}
