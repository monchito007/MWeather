package com.monchito007.mweather.data.cache

import com.monchito007.mweather.data.dto.CachedForecastDto
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Stores only the last downloaded forecast: one location, overwritten on every download. */
interface ForecastCache {
    /** The saved forecast, or null if there is none or it can't be read. Never throws. */
    suspend fun read(): CachedForecastDto?

    suspend fun write(entry: CachedForecastDto)
}

/**
 * Saves the forecast as JSON in a file, reusing the API's Moshi adapters. A JSON file is enough
 * for a single entry; Room would only be worth it for several cities (see docs/ROADMAP.md).
 */
class FileForecastCache(
    private val file: File,
    moshi: Moshi = Moshi.Builder().build(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ForecastCache {

    private val adapter = moshi.adapter(CachedForecastDto::class.java)

    override suspend fun read(): CachedForecastDto? = withContext(ioDispatcher) {
        try {
            if (file.exists()) adapter.fromJson(file.readText()) else null
        } catch (_: IOException) {
            null // unreadable or malformed JSON (JsonEncodingException is an IOException)
        } catch (_: JsonDataException) {
            null // valid JSON with an unexpected shape, e.g. written by an older app version
        }
    }

    override suspend fun write(entry: CachedForecastDto) = withContext(ioDispatcher) {
        // Write to a temp file and rename, so a crash mid-write never leaves a half-written cache.
        val temp = File(file.parentFile, "${file.name}.tmp")
        temp.writeText(adapter.toJson(entry))
        if (!temp.renameTo(file)) {
            file.delete()
            if (!temp.renameTo(file)) throw IOException("Could not save the forecast cache")
        }
    }
}
