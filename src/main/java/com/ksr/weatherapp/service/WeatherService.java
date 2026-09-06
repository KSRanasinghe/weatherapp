package com.ksr.weatherapp.service;

import com.ksr.weatherapp.dto.CachedWeather;
import com.ksr.weatherapp.dto.CityComfortResult;
import com.ksr.weatherapp.dto.external.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<String, CachedWeather> cache = new HashMap<>();
    private final Map<String, String> cacheStatus = new HashMap<>(); // cityCode -> "HIT" or "MISS"

    private static final long CACHE_DURATION_MS = 5 * 60 * 1000;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<CityComfortResult> getAllCityComfort() {
        List<CityComfortResult> results = new ArrayList<>();

        for (String cityCode : citycodes) {
            WeatherResponse response = getWeatherForCity(cityCode);

            CityComfortResult result = new CityComfortResult();
            result.setId(response.getId());
            result.setCityName(response.getName());
            result.setDescription(response.getWeather().getFirst().getDescription());
            result.setTemp(response.getMain().getTemp());
            result.setComfortScore(calculateComfortScore(response));

            results.add(result);
        }

        results.sort((a,b) -> Double.compare(b.getComfortScore(), a.getComfortScore()));

        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        return results;
    }

    private WeatherResponse getWeatherForCity(String cityCode) {
        long now =  System.currentTimeMillis();
        CachedWeather cached = cache.get(cityCode);

        if(cached != null && (now - cached.getTimestamp()) <  CACHE_DURATION_MS) {
            cacheStatus.put(cityCode, "HIT");
            return cached.getWeatherResponse();
        }

        cacheStatus.put(cityCode, "MISS");

        String url = "https://api.openweathermap.org/data/2.5/weather?id="
                + cityCode + "&appid=" + apiKey + "&units=metric";

        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
        cache.put(cityCode, new CachedWeather(response, now));

        return response;
    }

    private double calculateComfortScore(WeatherResponse response) {
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

    public Map<String, String> getCacheStatus(){
        return cacheStatus;
    }
}
