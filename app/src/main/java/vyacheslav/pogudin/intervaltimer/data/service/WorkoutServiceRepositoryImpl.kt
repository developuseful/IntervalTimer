package vyacheslav.pogudin.intervaltimer.data.service

import kotlinx.coroutines.flow.StateFlow
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.service.TimerForegroundService
import vyacheslav.pogudin.intervaltimer.timer.TimerState

/**
 * Реализация репозитория для работы с сервисом тренировки
 * Адаптирует TimerForegroundService к clean architecture интерфейсу
 */
class WorkoutServiceRepositoryImpl : WorkoutServiceRepository {
    
    // Внутренняя переменная сервиса - может обновляться
    private var service: TimerForegroundService? = null
    
    /**
     * Установить экземпляр сервиса
     * Вызывается когда сервис подключается
     */
    fun setService(timerService: TimerForegroundService) {
        service = timerService
    }
    
    override fun setTimer(timer: Timer) {
        service?.setTimer(timer)
    }
    
    override fun startTimer() {
        service?.startTimer()
    }
    
    override fun pauseTimer() {
        service?.pauseTimer()
    }
    
    override fun resumeTimer() {
        service?.resumeTimer()
    }
    
    override fun resetTimer() {
        service?.resetTimer()
    }
    
    override fun stopService() {
        service?.stopSelf()
    }
    
    override fun getTimerState(): StateFlow<TimerState> {
        return service?.timerState ?: throw IllegalStateException("Service is not bound")
    }
}
