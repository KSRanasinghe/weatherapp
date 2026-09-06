package com.ksr.weatherapp.dto;

import lombok.Data;

@Data
public class CityComfortResult {
    private int id;
    private String cityName;
    private String description;
    private double temp;
    private double comfortScore;
    private int rank;
}
