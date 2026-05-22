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
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounter: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _sessionSteps = MutableStateFlow(0)
    val sessionSteps: StateFlow<Int> = _sessionSteps.asStateFlow()

    private val _stepBaseline = MutableStateFlow<Float?>(null)
    val stepBaseline: StateFlow<Float?> = _stepBaseline.asStateFlow()
    private var latestTotal: Float? = null

    private var isRegistered = false
    val isAvailable: Boolean get() = stepCounter != null

    fun start() {
        if (isRegistered) return
        val sensor = stepCounter ?: return
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isRegistered = true
    }

    fun currentTotalSteps(): Float? = latestTotal
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        val total = event.values[0]
        latestTotal = total

        if (_stepBaseline.value == null) {
            _stepBaseline.value = total
        }

        recomputeSessionSteps()
    }

    fun setBaseline(value: Float?) {
        _stepBaseline.value = value
        recomputeSessionSteps()
    }

    fun stop() {
        if (!isRegistered) return
        sensorManager.unregisterListener(this)
        isRegistered = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun recomputeSessionSteps() {
        val total = latestTotal ?: return
        val base = _stepBaseline.value ?: return
        _sessionSteps.value = (total - base).toInt().coerceAtLeast(0)
    }
}