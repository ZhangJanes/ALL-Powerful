package com.allpowerful.backend.weather;

import com.allpowerful.backend.common.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 按预设城市取天气；同城共享 Redis 缓存。
 * 每天 4 个时段：夜间 1 次（0–6）+ 白天 3 次（6–12 / 12–18 / 18–24）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final int FORECAST_DAYS = 5;
    private static final String CACHE_PREFIX = "weather:v1:";

    private final WeatherProperties properties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public WeatherDtos.WeatherResponse forecastForCity(String city) {
        WeatherCities.Location location = WeatherCities.orDefault(city);
        ZoneId zone = ZoneId.of(properties.timezone());
        ZonedDateTime now = ZonedDateTime.now(zone);
        int slot = currentSlot(now.getHour());
        String cacheKey = CACHE_PREFIX + location.city() + ":" + now.toLocalDate() + ":" + slot;

        WeatherDtos.WeatherResponse cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        JsonNode root = fetchForecast(location.latitude(), location.longitude());
        WeatherDtos.CurrentWeather current = parseCurrent(root.path("current"));
        List<WeatherDtos.DailyForecast> daily = parseDaily(root.path("daily"), now.toLocalDate());
        String summary = current.description() + " " + formatTemp(current.temperature());

        WeatherDtos.WeatherResponse response = new WeatherDtos.WeatherResponse(
                location.city(),
                location.latitude(),
                location.longitude(),
                properties.timezone(),
                summary,
                current,
                daily
        );
        writeCache(cacheKey, response, ttlUntilSlotEnd(now, slot));
        return response;
    }

    /** 0=夜间, 1=上午, 2=下午, 3=晚间 */
    static int currentSlot(int hour) {
        if (hour < 6) return 0;
        if (hour < 12) return 1;
        if (hour < 18) return 2;
        return 3;
    }

    private static Duration ttlUntilSlotEnd(ZonedDateTime now, int slot) {
        LocalDate date = now.toLocalDate();
        ZonedDateTime end = switch (slot) {
            case 0 -> date.atTime(LocalTime.of(6, 0)).atZone(now.getZone());
            case 1 -> date.atTime(LocalTime.of(12, 0)).atZone(now.getZone());
            case 2 -> date.atTime(LocalTime.of(18, 0)).atZone(now.getZone());
            default -> date.plusDays(1).atStartOfDay(now.getZone());
        };
        Duration ttl = Duration.between(now, end);
        if (ttl.isNegative() || ttl.isZero()) {
            return Duration.ofMinutes(5);
        }
        // 略提前过期，避免卡在时段边界
        Duration trimmed = ttl.minusSeconds(10);
        return trimmed.isNegative() || trimmed.isZero() ? Duration.ofMinutes(1) : trimmed;
    }

    private WeatherDtos.WeatherResponse readCache(String key) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, WeatherDtos.WeatherResponse.class);
        } catch (Exception ex) {
            log.warn("读取天气缓存失败: {}", ex.getMessage());
            return null;
        }
    }

    private void writeCache(String key, WeatherDtos.WeatherResponse value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            log.warn("写入天气缓存失败: {}", ex.getMessage());
        }
    }

    private JsonNode fetchForecast(double latitude, double longitude) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(properties.forecastUrl())
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m")
                    .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                    .queryParam("forecast_days", FORECAST_DAYS)
                    .queryParam("timezone", properties.timezone())
                    .build(true)
                    .toUri();
            return getJson(uri);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AppException("获取天气失败，请稍后重试");
        }
    }

    private WeatherDtos.CurrentWeather parseCurrent(JsonNode current) {
        int code = current.path("weather_code").asInt(0);
        double temperature = current.path("temperature_2m").asDouble(0);
        Integer humidity = current.hasNonNull("relative_humidity_2m")
                ? current.path("relative_humidity_2m").asInt()
                : null;
        Double windSpeed = current.hasNonNull("wind_speed_10m")
                ? current.path("wind_speed_10m").asDouble()
                : null;
        return new WeatherDtos.CurrentWeather(
                WeatherCodeMapper.toDescription(code),
                code,
                round1(temperature),
                humidity,
                windSpeed == null ? null : round1(windSpeed)
        );
    }

    private List<WeatherDtos.DailyForecast> parseDaily(JsonNode daily, LocalDate today) {
        JsonNode dates = daily.path("time");
        JsonNode codes = daily.path("weather_code");
        JsonNode maxTemps = daily.path("temperature_2m_max");
        JsonNode minTemps = daily.path("temperature_2m_min");
        JsonNode precip = daily.path("precipitation_probability_max");

        List<WeatherDtos.DailyForecast> list = new ArrayList<>();
        int size = dates.isArray() ? Math.min(FORECAST_DAYS, dates.size()) : 0;
        for (int i = 0; i < size; i++) {
            LocalDate date = LocalDate.parse(dates.get(i).asText());
            int code = codes.path(i).asInt(0);
            Integer precipProb = precip.path(i).isMissingNode() || precip.path(i).isNull()
                    ? null
                    : precip.path(i).asInt();
            list.add(new WeatherDtos.DailyForecast(
                    date.toString(),
                    weekdayLabel(date, today),
                    WeatherCodeMapper.toDescription(code),
                    code,
                    round1(maxTemps.path(i).asDouble(0)),
                    round1(minTemps.path(i).asDouble(0)),
                    precipProb
            ));
        }
        return list;
    }

    private JsonNode getJson(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new AppException("天气服务暂时不可用");
        }
        return objectMapper.readTree(response.body());
    }

    private static String weekdayLabel(LocalDate date, LocalDate today) {
        if (date.equals(today)) {
            return "今天";
        }
        if (date.equals(today.plusDays(1))) {
            return "明天";
        }
        DayOfWeek day = date.getDayOfWeek();
        return day.getDisplayName(TextStyle.SHORT, Locale.CHINA);
    }

    private static String formatTemp(double temperature) {
        return Math.round(temperature) + "℃";
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
