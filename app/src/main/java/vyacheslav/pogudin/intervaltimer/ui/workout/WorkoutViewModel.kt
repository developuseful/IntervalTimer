package vyacheslav.pogudin.intervaltimer.ui.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.timer.TimerEngine

enum class WorkoutPhase {
    Ready,
    Running,
    Paused,
    Finished
}

class WorkoutViewModel(val timer: Timer) : ViewModel() {
    private val engine = TimerEngine()

    var elapsed by mutableStateOf(0)
    var running by mutableStateOf(false)

    val phase: WorkoutPhase
        get() = when {
            timer.totalTime > 0 && elapsed >= timer.totalTime -> WorkoutPhase.Finished
            running -> WorkoutPhase.Running
            elapsed > 0 -> WorkoutPhase.Paused
            else -> WorkoutPhase.Ready
        }

    init {
        viewModelScope.launch {
            while (true) {
                delay(100)
                val rawSeconds = (engine.elapsed() / 1000).toInt()
                elapsed = min(rawSeconds, timer.totalTime)
                if (timer.totalTime > 0 && rawSeconds >= timer.totalTime && running) {
                    engine.pause()
                    running = false
                }
            }
        }
    }

    fun start() {
        engine.start()
        running = true
    }

    fun pause() {
        engine.pause()
        running = false
    }

    fun resume() {
        engine.resume()
        running = true
    }

    fun reset() {
        engine.reset()
        running = false
        elapsed = 0
    }
}