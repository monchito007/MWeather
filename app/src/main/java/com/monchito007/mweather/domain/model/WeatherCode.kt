package com.monchito007.mweather.domain.model

enum class WeatherCategory {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM,
    UNKNOWN
}

data class WeatherCodeInfo(
    val code: Int,
    val description: String,
    val category: WeatherCategory
) {
    companion object {
        fun fromCode(code: Int): WeatherCodeInfo {
            return when (code) {
                0 -> WeatherCodeInfo(0, "Clear sky", WeatherCategory.CLEAR)
                1 -> WeatherCodeInfo(1, "Mainly clear", WeatherCategory.CLEAR)
                2 -> WeatherCodeInfo(2, "Partly cloudy", WeatherCategory.PARTLY_CLOUDY)
                3 -> WeatherCodeInfo(3, "Overcast", WeatherCategory.CLOUDY)
                45 -> WeatherCodeInfo(45, "Foggy", WeatherCategory.FOG)
                48 -> WeatherCodeInfo(48, "Depositing rime fog", WeatherCategory.FOG)
                51 -> WeatherCodeInfo(51, "Light drizzle", WeatherCategory.DRIZZLE)
                53 -> WeatherCodeInfo(53, "Moderate drizzle", WeatherCategory.DRIZZLE)
                55 -> WeatherCodeInfo(55, "Dense drizzle", WeatherCategory.DRIZZLE)
                56 -> WeatherCodeInfo(56, "Light freezing drizzle", WeatherCategory.DRIZZLE)
                57 -> WeatherCodeInfo(57, "Dense freezing drizzle", WeatherCategory.DRIZZLE)
                61 -> WeatherCodeInfo(61, "Slight rain", WeatherCategory.RAIN)
                63 -> WeatherCodeInfo(63, "Moderate rain", WeatherCategory.RAIN)
                65 -> WeatherCodeInfo(65, "Heavy rain", WeatherCategory.RAIN)
                66 -> WeatherCodeInfo(66, "Light freezing rain", WeatherCategory.RAIN)
                67 -> WeatherCodeInfo(67, "Heavy freezing rain", WeatherCategory.RAIN)
                71 -> WeatherCodeInfo(71, "Slight snow fall", WeatherCategory.SNOW)
                73 -> WeatherCodeInfo(73, "Moderate snow fall", WeatherCategory.SNOW)
                75 -> WeatherCodeInfo(75, "Heavy snow fall", WeatherCategory.SNOW)
                77 -> WeatherCodeInfo(77, "Snow grains", WeatherCategory.SNOW)
                80 -> WeatherCodeInfo(80, "Slight rain showers", WeatherCategory.RAIN)
                81 -> WeatherCodeInfo(81, "Moderate rain showers", WeatherCategory.RAIN)
                82 -> WeatherCodeInfo(82, "Violent rain showers", WeatherCategory.RAIN)
                85 -> WeatherCodeInfo(85, "Slight snow showers", WeatherCategory.SNOW)
                86 -> WeatherCodeInfo(86, "Heavy snow showers", WeatherCategory.SNOW)
                95 -> WeatherCodeInfo(95, "Thunderstorm", WeatherCategory.THUNDERSTORM)
                96 -> WeatherCodeInfo(96, "Thunderstorm with hail", WeatherCategory.THUNDERSTORM)
                99 -> WeatherCodeInfo(99, "Severe thunderstorm with hail", WeatherCategory.THUNDERSTORM)
                else -> WeatherCodeInfo(code, "Unknown weather", WeatherCategory.UNKNOWN)
            }
        }
    }
}
