package vyacheslav.pogudin.intervaltimer.domain.usecases

import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepository

/**
 * Юзкейс для паузы тренировки
 */
class PauseWorkoutUseCase(private val workoutService: WorkoutServiceRepository) {
    
    operator fun invoke() {
        workoutService.pauseTimer()
    }
}
