package com.ksr.weatherapp.dto;

import lombok.Data;

@Data
public class CityComfortResult {
    private String cityName;
    private String description;
    private double temp;
    private double comfortScore;
}
