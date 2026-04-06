package vyacheslav.pogudin.intervaltimer.ui.workout

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.service.TimerForegroundService
import vyacheslav.pogudin.intervaltimer.timer.TimerState
import vyacheslav.pogudin.intervaltimer.service.WorkoutPhase

class WorkoutViewModel(
    application: Application,
    val timer: Timer
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var isBound = false
    private var service: TimerForegroundService? = null

    // Исправленная реализация ServiceConnection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            this@WorkoutViewModel.service = binder.getService()
            this@WorkoutViewModel.service?.setTimer(timer)
            isBound = true
            startObservingService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(getApplication(), TimerForegroundService::class.java)
        getApplication<Application>().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        getApplication<Application>().startService(intent)
    }

    private fun startObservingService() {
        viewModelScope.launch {
            service?.timerState?.collect { state ->
                updateUiFromState(state)
            }
        }
    }

    private fun updateUiFromState(state: TimerState) {
        _uiState.value = WorkoutUiState(
            elapsed = state.elapsedSeconds,
            isRunning = state.isRunning,
            phase = when {
                timer.totalTime > 0 && state.elapsedSeconds >= timer.totalTime -> WorkoutPhase.Finished
                state.isRunning -> WorkoutPhase.Running
                state.elapsedSeconds > 0 -> WorkoutPhase.Paused
                else -> WorkoutPhase.Ready
            },
            currentIntervalIndex = state.currentIntervalIndex,
            remainingInInterval = state.remainingInInterval
        )
    }

    fun start() {
        service?.startTimer()
    }

    fun pause() {
        service?.pauseTimer()
    }

    fun resume() {
        service?.resumeTimer()
    }

    fun reset() {
        service?.resetTimer()
    }

    fun stopAndUnbind() {
        try {
            getApplication<Application>().stopService(
                Intent(getApplication(), TimerForegroundService::class.java)
            )
            if (isBound) {
                getApplication<Application>().unbindService(connection)
                isBound = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        service = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAndUnbind()
    }
}

data class WorkoutUiState(
    val elapsed: Int = 0,
    val isRunning: Boolean = false,
    val phase: WorkoutPhase = WorkoutPhase.Ready,
    val currentIntervalIndex: Int = 0,
    val remainingInInterval: Int = 0
)