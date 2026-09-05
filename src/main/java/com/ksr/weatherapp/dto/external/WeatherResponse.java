package com.ksr.weatherapp.dto.external;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    private int id;
    private String name;
    private Main main;
    private Wind wind;
    private Clouds clouds;
    private int visibility;
    private List<WeatherInfo> weather;
}
