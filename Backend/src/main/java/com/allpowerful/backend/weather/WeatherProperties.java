package com.allpowerful.backend.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.weather")
public record WeatherProperties(
        String forecastUrl,
        String geocodeUrl,
        String defaultCity,
        double defaultLatitude,
        double defaultLongitude,
        String timezone
) {}
