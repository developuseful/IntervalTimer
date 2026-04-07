package vyacheslav.pogudin.intervaltimer.domain.usecases

import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepository

/**
 * Юзкейс для возобновления тренировки после паузы
 */
class ResumeWorkoutUseCase(private val workoutService: WorkoutServiceRepository) {
    
    operator fun invoke() {
        workoutService.resumeTimer()
    }
}
