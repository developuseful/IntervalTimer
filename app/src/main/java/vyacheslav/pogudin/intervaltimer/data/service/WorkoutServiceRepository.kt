package vyacheslav.pogudin.intervaltimer.data.service

import kotlinx.coroutines.flow.StateFlow
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.timer.TimerState

/**
 * Репозиторий-абстракция для работы с сервисом тренировки
 * Предоставляет clean architecture интерфейс для взаимодействия с TimerForegroundService
 */
interface WorkoutServiceRepository {
    
    /**
     * Установить таймер для тренировки
     */
    fun setTimer(timer: Timer)
    
    /**
     * Запустить таймер
     */
    fun startTimer()
    
    /**
     * Поставить таймер на паузу
     */
    fun pauseTimer()
    
    /**
     * Возобновить таймер после паузы
     */
    fun resumeTimer()
    
    /**
     * Сбросить таймер
     */
    fun resetTimer()
    
    /**
     * Остановить сервис
     */
    fun stopService()
    
    /**
     * Получить поток состояния таймера
     */
    fun getTimerState(): StateFlow<TimerState>
}
