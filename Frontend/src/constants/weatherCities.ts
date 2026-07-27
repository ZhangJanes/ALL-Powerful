export const WEATHER_CITIES = ['北京', '上海', '重庆', '天津'] as const

export type WeatherCity = (typeof WEATHER_CITIES)[number]

/** 请求参数用拼音，避免 URL 中文未被编码时被 Tomcat 拒绝 */
export const WEATHER_CITY_CODES: Record<WeatherCity, string> = {
  北京: 'beijing',
  上海: 'shanghai',
  重庆: 'chongqing',
  天津: 'tianjin',
}

export function isWeatherCity(value: unknown): value is WeatherCity {
  return typeof value === 'string' && (WEATHER_CITIES as readonly string[]).includes(value)
}
