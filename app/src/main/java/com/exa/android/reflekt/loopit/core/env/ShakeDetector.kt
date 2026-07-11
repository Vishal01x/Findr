package com.exa.android.reflekt.loopit.core.env

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlin.math.sqrt

class ShakeDetector(context: Context, private val onShake: () -> Unit) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastShakeTime = 0L
    private val shakeCooldownMs = 1_000L
    private val threshold = 12f

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gForce = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            if (gForce > threshold) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > shakeCooldownMs) {
                    lastShakeTime = now
                    // Compose state must be written on the main thread
                    mainHandler.post { onShake() }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
