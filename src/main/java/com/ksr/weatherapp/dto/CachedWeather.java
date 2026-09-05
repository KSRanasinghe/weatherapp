package com.ksr.weatherapp.dto;

import com.ksr.weatherapp.dto.external.WeatherResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CachedWeather {
    private WeatherResponse weatherResponse;
    private long timestamp;
}
