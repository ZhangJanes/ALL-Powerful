package com.allpowerful.backend.weather;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import com.allpowerful.backend.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;
    private final SettingsService settingsService;

    /** 按当前用户设置的城市返回天气（同城共享时段缓存） */
    @GetMapping
    public ApiResponse<WeatherDtos.WeatherResponse> forecast() {
        String city = settingsService.getWeatherCity(CurrentUser.id());
        return ApiResponse.ok(weatherService.forecastForCity(city));
    }

    /**
     * 登录页等未登录场景。
     * city 支持中文或拼音 code（beijing/shanghai/chongqing/tianjin），默认北京。
     */
    @GetMapping("/public")
    public ApiResponse<WeatherDtos.WeatherResponse> publicForecast(
            @RequestParam(value = "city", required = false) String city
    ) {
        return ApiResponse.ok(weatherService.forecastForCity(WeatherCities.orDefault(city).city()));
    }
}
