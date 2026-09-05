package com.ksr.weatherapp.dto.external;

import lombok.Data;

@Data
public class Main {
    private double temp;
    private int humidity;
    private int pressure;
}
