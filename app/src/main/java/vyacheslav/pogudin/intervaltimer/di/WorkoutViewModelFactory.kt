package vyacheslav.pogudin.intervaltimer.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutViewModel

/**
 * Factory для создания WorkoutViewModel с инъекцией зависимостей
 */
class WorkoutViewModelFactory(
    private val application: Application,
    private val timer: Timer
) : ViewModelProvider.Factory {
    
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(
                application = application,
                timer = timer,
                startWorkoutUseCase = AppContainer.getStartWorkoutUseCase(),
                stopWorkoutUseCase = AppContainer.getStopWorkoutUseCase(),
                pauseWorkoutUseCase = AppContainer.getPauseWorkoutUseCase(),
                resumeWorkoutUseCase = AppContainer.getResumeWorkoutUseCase()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
