package com.allpowerful.backend.weather;

final class WeatherCodeMapper {
    private WeatherCodeMapper() {}

    static String toDescription(int code) {
        if (code == 0) return "晴";
        if (code == 1) return "大部晴朗";
        if (code == 2) return "多云";
        if (code == 3) return "阴";
        if (code == 45 || code == 48) return "雾";
        if (code >= 51 && code <= 57) return "毛毛雨";
        if (code >= 61 && code <= 67) return "雨";
        if (code >= 71 && code <= 77) return "雪";
        if (code >= 80 && code <= 82) return "阵雨";
        if (code == 85 || code == 86) return "阵雪";
        if (code >= 95 && code <= 99) return "雷阵雨";
        return "未知";
    }
}
