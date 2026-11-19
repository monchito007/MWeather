package com.monchito007.mweather

import com.monchito007.mweather.data.dto.GeocodingSearchResponseDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Parses API-shaped JSON with a Moshi instance that has NO reflection adapter, exactly like
 * ApiClient. If a DTO loses its @JsonClass(generateAdapter = true), these tests fail.
 */
class DtoParsingTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun parsesForecastResponse() {
        val json = """
            {
              "latitude": 40.42, "longitude": -3.7, "timezone": "Europe/Madrid",
              "current": {
                "time": "2026-11-19T12:00", "temperature_2m": 14.3, "relative_humidity_2m": 61,
                "apparent_temperature": 12.9, "is_day": 1, "precipitation": 0.0, "weather_code": 2,
                "cloud_cover": 40, "pressure_msl": 1019.4, "wind_speed_10m": 9.7, "wind_direction_10m": 250
              },
              "hourly": {
                "time": ["2026-11-19T00:00", "2026-11-19T01:00"],
                "temperature_2m": [9.1, null],
                "weather_code": [0, 3],
                "is_day": [0, 0]
              },
              "daily": {
                "time": ["2026-11-19"],
                "weather_code": [2],
                "temperature_2m_max": [16.0],
                "temperature_2m_min": [7.5],
                "uv_index_max": [2.4],
                "sunrise": ["2026-11-19T08:05"],
                "sunset": ["2026-11-19T17:52"]
              },
              "generationtime_ms": 0.05
            }
        """.trimIndent()

        val dto = moshi.adapter(OpenMeteoForecastDto::class.java).fromJson(json)!!

        assertEquals("Europe/Madrid", dto.timezone)
        assertEquals(14.3, dto.current?.temperature2m ?: 0.0, 0.001)
        assertEquals(2, dto.current?.weatherCode)
        assertEquals(61.0, dto.current?.relativeHumidity2m ?: 0.0, 0.001)
        assertEquals(listOf(9.1, null), dto.hourly?.temperature2m)
        assertNull(dto.hourly?.precipitation)
        assertEquals(16.0, dto.daily?.temperature2mMax?.first() ?: 0.0, 0.001)
        assertEquals("2026-11-19T17:52", dto.daily?.sunset?.first())
    }

    @Test
    fun parsesGeocodingResponse() {
        val json = """
            {
              "results": [
                { "id": 2643743, "name": "Londres", "latitude": 51.50853, "longitude": -0.12574,
                  "country": "Reino Unido", "admin1": "Inglaterra", "timezone": "Europe/London",
                  "population": 8961989 }
              ],
              "generationtime_ms": 0.6
            }
        """.trimIndent()

        val dto = moshi.adapter(GeocodingSearchResponseDto::class.java).fromJson(json)!!

        val london = dto.results!!.single()
        assertEquals(2643743L, london.id)
        assertEquals("Londres", london.name)
        assertEquals("Inglaterra", london.admin1)
    }

    @Test
    fun parsesEmptyGeocodingResponse() {
        val dto = moshi.adapter(GeocodingSearchResponseDto::class.java)
            .fromJson("""{ "generationtime_ms": 0.3 }""")!!

        assertNull(dto.results)
    }
}
