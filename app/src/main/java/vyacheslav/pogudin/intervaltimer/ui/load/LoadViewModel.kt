package vyacheslav.pogudin.intervaltimer.ui.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import vyacheslav.pogudin.intervaltimer.data.repository.StubTimer
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

class LoadViewModel(private val repo: TimerRepository) : ViewModel() {
    var id by mutableStateOf("68")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var timer by mutableStateOf<Timer?>(null)
    var useTestWorkout by mutableStateOf(StubTimer.ENABLED)

    fun load() {
        viewModelScope.launch {
            loading = true
            delay(1000)
            error = null
            try {
                timer = repo.getTimer(id.toInt())
            } catch (e: Exception) {
                error = "Тренировка не найдена. Проверьте ID."
            } finally {
                loading = false
            }
        }
    }

    fun clearError() {
        error = null
    }

    fun updateTestWorkout(enabled: Boolean) {
        useTestWorkout = enabled
        StubTimer.ENABLED = enabled
    }
}
