package com.pham0326.flinders.zootreasurehunt.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LightSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    companion object {

        private const val DARK_ENTER_LUX = 8f
        private const val DARK_EXIT_LUX = 15f
        private const val INITIAL_LUX = 100f
    }

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val _currentLux = MutableStateFlow(INITIAL_LUX)
    val currentLux: StateFlow<Float> = _currentLux.asStateFlow()
    private val _isNocturnal = MutableStateFlow(false)
    val isNocturnal: StateFlow<Boolean> = _isNocturnal.asStateFlow()
    private var isRegistered = false
    val isAvailable: Boolean get() = lightSensor != null

    fun start() {
        if (isRegistered) return
        val sensor = lightSensor ?: return
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        sensorManager.unregisterListener(this)
        isRegistered = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_LIGHT) return

        val lux = event.values[0]
        _currentLux.value = lux

        val wasNocturnal = _isNocturnal.value
        val nowNocturnal = when {
            wasNocturnal && lux > DARK_EXIT_LUX -> false
            !wasNocturnal && lux < DARK_ENTER_LUX -> true
            else -> wasNocturnal
        }

        if (nowNocturnal != wasNocturnal) {
            _isNocturnal.value = nowNocturnal
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}