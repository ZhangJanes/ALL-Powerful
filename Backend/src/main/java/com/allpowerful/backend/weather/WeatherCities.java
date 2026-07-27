package com.allpowerful.backend.weather;

import com.allpowerful.backend.common.AppException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 预设城市（直辖市）及坐标，避免每次地理编码 */
public final class WeatherCities {
    public static final String DEFAULT_CITY = "北京";

    private static final Map<String, Location> PRESETS = new LinkedHashMap<>();
    private static final Map<String, String> CODE_TO_CITY = new LinkedHashMap<>();

    static {
        PRESETS.put("北京", new Location("北京", 39.9042, 116.4074));
        PRESETS.put("上海", new Location("上海", 31.2304, 121.4737));
        PRESETS.put("重庆", new Location("重庆", 29.5630, 106.5516));
        PRESETS.put("天津", new Location("天津", 39.3434, 117.3616));

        CODE_TO_CITY.put("beijing", "北京");
        CODE_TO_CITY.put("shanghai", "上海");
        CODE_TO_CITY.put("chongqing", "重庆");
        CODE_TO_CITY.put("tianjin", "天津");
    }

    private WeatherCities() {}

    public static Set<String> names() {
        return PRESETS.keySet();
    }

    public static List<String> list() {
        return List.copyOf(PRESETS.keySet());
    }

    public static boolean isSupported(String city) {
        return resolveName(city) != null;
    }

    public static Location require(String city) {
        String name = resolveName(city);
        if (name == null) {
            throw new AppException("仅支持城市：北京、上海、重庆、天津");
        }
        return PRESETS.get(name);
    }

    public static Location orDefault(String city) {
        String name = resolveName(city);
        if (name == null) {
            return PRESETS.get(DEFAULT_CITY);
        }
        return PRESETS.get(name);
    }

    /** 支持中文名或拼音 code（beijing/shanghai/chongqing/tianjin） */
    public static String resolveName(String cityOrCode) {
        if (cityOrCode == null) {
            return null;
        }
        String raw = cityOrCode.trim();
        if (raw.isEmpty()) {
            return null;
        }
        if (PRESETS.containsKey(raw)) {
            return raw;
        }
        return CODE_TO_CITY.get(raw.toLowerCase(Locale.ROOT));
    }

    public record Location(String city, double latitude, double longitude) {}
}
