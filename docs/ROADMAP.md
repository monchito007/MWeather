# Hoja de ruta de MWeather

Propuestas de mejora pendientes. Última revisión: 24/11/2026.

## Funcionalidades nuevas

- [x] **Ciudades favoritas**: guardar varias ciudades y pasar de una a otra deslizando. Se guardan con DataStore (como JSON), sin Room: son pocas ciudades y no hacía falta una base de datos. El tiempo de las favoritas no sustituye a la caché offline ni al widget.
- [x] **Caché offline**: guardar el último pronóstico y mostrarlo sin conexión, con un indicador de "actualizado hace X min". Guardado como JSON en `files/last_forecast.json`.
- [x] **Widget** para la pantalla de inicio (Jetpack Glance). Usa la caché, así que funciona sin conexión.
- [x] **Calidad del aire y polen** con la [API de Air Quality de Open-Meteo](https://open-meteo.com/en/docs/air-quality-api) (gratuita). Índice europeo, PM2.5, PM10, NO₂ y O₃; el polen solo aparece en Europa y en temporada.
- [ ] **Alertas de lluvia** mediante notificaciones (WorkManager).
- [ ] **Diseño para tablet y plegables**. Requiere volver a añadir Material3 Adaptive (se eliminó por no usarse).
- [ ] **Flecha de dirección del viento en todas las indicaciones de viento**: hoy solo la lleva la tarjeta "Viento" de las métricas detalladas. Faltaría en la tarjeta principal (ya hay dato), en cada hora (pedir `wind_direction_10m` por hora) y, si se muestra el viento por día, con `wind_direction_10m_dominant`. Reutilizar el componente `WindCompass`.

## Mejoras técnicas

- [ ] **Mover `TemperatureUnit`, `AppLanguage` y `ThemeMode` a `domain`**. Ahora están en `ui.weather` y la capa de datos (`data/preferences`) los importa desde la interfaz.
- [ ] **Idioma del sistema en el primer arranque**. Si no hay idioma guardado, la app arranca siempre en español, aunque el móvil esté en inglés.
- [ ] **Medir el rendimiento de `ColorfulWeatherIcon`**. Cada redibujado vuelve a crear los `Path` y `Brush`. Si se confirma que es un problema, valorar `drawWithCache`; no se aplicó porque añade complejidad sin una mejora medida.
- [ ] **Test de extremo a extremo** que arranque la app completa. Habría que simular la red y el permiso de ubicación para que no falle de forma intermitente.

### Descartado por ahora

- **Subir `minSdk` a 26 o activar `coreLibraryDesugaring`**. Las fechas ya usan `Calendar` y funcionan desde Android 7 (API 24); subir el mínimo dejaría fuera esos dispositivos sin ganar nada concreto.

## Hecho

Mejoras de la revisión inicial, ya en `main`:

- Errores de red visibles en lugar de datos inventados
- Geocodificación inversa fuera del hilo principal
- Una sola carga al arrancar (también al rotar la pantalla)
- Logging HTTP solo en builds de depuración
- Preferencias y última ciudad guardadas con DataStore
- Textos en `strings.xml` (inglés y español) con selector de idioma en la app
- Dependencias sin usar eliminadas y versiones fijas
- Moshi con adaptadores generados, sin reflexión
- "—" para los datos que faltan en lugar de ceros
- Contenedor de dependencias manual (`di/AppContainer`)
- Tests de interfaz con Compose y limpieza de los iconos del tiempo

Resultado: el APK de release pasó de 4,9 MB a 3 MB.
