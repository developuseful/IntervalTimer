package vyacheslav.pogudin.intervaltimer.domain.usecases

import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepository

/**
 * Юзкейс для остановки и сброса тренировки
 * Инкапсулирует логику остановки таймера и очистки состояния
 */
class StopWorkoutUseCase(private val workoutService: WorkoutServiceRepository) {
    
    operator fun invoke() {
        workoutService.resetTimer()
        workoutService.stopService()
    }
}
