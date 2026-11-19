package com.monchito007.mweather

import com.monchito007.mweather.domain.model.WeatherCodeInfo
import com.monchito007.mweather.ui.weather.components.WeatherIconMapper
import com.monchito007.mweather.ui.weather.components.WeatherIconType
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherIconMapperTest {

    private fun icon(code: Int, isDay: Boolean = true) = WeatherIconMapper.getIconType(code, isDay)

    @Test
    fun clearAndPartlyCloudyDependOnDayOrNight() {
        assertEquals(WeatherIconType.CLEAR_DAY, icon(0, isDay = true))
        assertEquals(WeatherIconType.CLEAR_NIGHT, icon(0, isDay = false))
        assertEquals(WeatherIconType.PARTLY_CLOUDY_DAY, icon(2, isDay = true))
        assertEquals(WeatherIconType.PARTLY_CLOUDY_NIGHT, icon(1, isDay = false))
    }

    @Test
    fun eachWmoGroupMapsToItsIcon() {
        assertEquals(WeatherIconType.CLOUDY, icon(3))
        assertEquals(WeatherIconType.FOG, icon(48))
        assertEquals(WeatherIconType.DRIZZLE, icon(57))
        assertEquals(WeatherIconType.RAIN, icon(82))
        assertEquals(WeatherIconType.SNOW, icon(86))
        assertEquals(WeatherIconType.THUNDERSTORM, icon(99))
    }

    @Test
    fun unknownCodeIsANeutralCloudNotASun() {
        assertEquals(WeatherIconType.CLOUDY, icon(WeatherCodeInfo.UNKNOWN_CODE, isDay = true))
        assertEquals(WeatherIconType.CLOUDY, icon(42, isDay = false))
    }
}
