package vyacheslav.pogudin.intervaltimer.ui.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.timer.TimerEngine

class WorkoutViewModel(val timer: Timer) : ViewModel() {
    private val engine = TimerEngine()

    var elapsed by mutableStateOf(0)
    var running by mutableStateOf(false)

    init {
        viewModelScope.launch {
            while (true) {
                delay(100)
                elapsed = (engine.elapsed() / 1000).toInt()
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