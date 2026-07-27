package com.allpowerful.backend.weather;

import java.util.List;

public final class WeatherDtos {
    private WeatherDtos() {}

    public record CurrentWeather(
            String description,
            int weatherCode,
            double temperature,
            Integer humidity,
            Double windSpeed
    ) {}

    public record DailyForecast(
            String date,
            String weekday,
            String description,
            int weatherCode,
            double tempMax,
            double tempMin,
            Integer precipProbability
    ) {}

    public record WeatherResponse(
            String city,
            double latitude,
            double longitude,
            String timezone,
            String summary,
            CurrentWeather current,
            List<DailyForecast> daily
    ) {}
}
