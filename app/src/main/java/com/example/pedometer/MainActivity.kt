package com.example.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PedometerScreen()
                }
            }
        }
    }
}

@Composable
private fun PedometerScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var steps by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableIntStateOf(R.string.waiting_for_data) }
    var baseline by remember { mutableFloatStateOf(-1f) }

    DisposableEffect(sensorManager) {
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            private var accelerationThreshold = 11.2f
            private var lastStepTime = 0L

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_STEP_COUNTER -> {
                        if (baseline < 0f) {
                            baseline = event.values[0]
                            statusText = R.string.hardware_step_counter
                        }
                        steps = (event.values[0] - baseline).toInt().coerceAtLeast(0)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        if (stepCounterSensor != null) return
                        statusText = R.string.fallback_accelerometer

                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                        val now = System.currentTimeMillis()
                        if (magnitude > accelerationThreshold && now - lastStepTime > 320) {
                            steps += 1
                            lastStepTime = now
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        when {
            stepCounterSensor != null -> {
                sensorManager.registerListener(
                    listener,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

            accelerometerSensor != null -> {
                sensorManager.registerListener(
                    listener,
                    accelerometerSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }

            else -> {
                statusText = R.string.sensor_unavailable
            }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🚶", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "${context.getString(R.string.steps_today)}: $steps", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = context.getString(statusText), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            baseline = -1f
            steps = 0
        }) {
            Text(text = context.getString(R.string.reset_counter))
        }
    }

    LaunchedEffect(Unit) {
        // Placeholder to keep the composable extensible for persistence in future.
    }
}
