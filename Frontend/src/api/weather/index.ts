import { get } from '../request'
import { WEATHER_CITY_CODES, type WeatherCity } from '@/constants/weatherCities'

export type WeatherCurrentDto = {
  description: string
  weatherCode: number
  temperature: number
  humidity: number | null
  windSpeed: number | null
}

export type WeatherDailyDto = {
  date: string
  weekday: string
  description: string
  weatherCode: number
  tempMax: number
  tempMin: number
  precipProbability: number | null
}

export type WeatherDto = {
  city: string
  latitude: number
  longitude: number
  timezone: string
  summary: string
  current: WeatherCurrentDto
  daily: WeatherDailyDto[]
}

/** 当前用户设置城市的天气 + 未来 5 天预报 */
export function fetchWeatherApi() {
  return get<WeatherDto>('/weather')
}

/** 登录页公开天气（无需登录；city 转拼音 code，避免中文 URL） */
export function fetchPublicWeatherApi(city?: WeatherCity | string) {
  const code =
    city && city in WEATHER_CITY_CODES
      ? WEATHER_CITY_CODES[city as WeatherCity]
      : undefined
  return get<WeatherDto>('/weather/public', code ? { city: code } : undefined, { withAuth: false })
}
