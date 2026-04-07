package vyacheslav.pogudin.intervaltimer.domain.usecases

import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

/**
 * Юзкейс для загрузки таймера по ID
 * Инкапсулирует логику получения данных таймера из репозитория
 */
class LoadTimerUseCase(private val repository: TimerRepository) {
    
    suspend operator fun invoke(timerId: Int): Timer {
        // Валидация ID
        if (timerId <= 0) {
            throw IllegalArgumentException("Timer ID must be positive")
        }
        
        return repository.getTimer(timerId)
    }
}
