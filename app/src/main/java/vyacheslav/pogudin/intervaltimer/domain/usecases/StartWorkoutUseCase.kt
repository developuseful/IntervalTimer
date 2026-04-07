package vyacheslav.pogudin.intervaltimer.domain.usecases

import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

/**
 * Юзкейс для запуска тренировки
 * Инкапсулирует логику инициализации и запуска таймера тренировки
 */
class StartWorkoutUseCase(private val workoutService: WorkoutServiceRepository) {
    
    operator fun invoke(timer: Timer) {
        workoutService.setTimer(timer)
        workoutService.startTimer()
    }
}
