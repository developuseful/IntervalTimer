package vyacheslav.pogudin.intervaltimer.di

import android.content.Context
import vyacheslav.pogudin.intervaltimer.data.api.ApiFactory
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepository
import vyacheslav.pogudin.intervaltimer.data.service.WorkoutServiceRepositoryImpl
import vyacheslav.pogudin.intervaltimer.domain.usecases.LoadTimerUseCase
import vyacheslav.pogudin.intervaltimer.domain.usecases.PauseWorkoutUseCase
import vyacheslav.pogudin.intervaltimer.domain.usecases.ResumeWorkoutUseCase
import vyacheslav.pogudin.intervaltimer.domain.usecases.StartWorkoutUseCase
import vyacheslav.pogudin.intervaltimer.domain.usecases.StopWorkoutUseCase
import vyacheslav.pogudin.intervaltimer.service.TimerForegroundService

/**
 * Service Locator для управления зависимостями
 * Позволяет централизованно создавать и управлять всеми объектами в приложении
 */
object AppContainer {
    
    private var _timerRepository: TimerRepository? = null
    private var _workoutServiceRepository: WorkoutServiceRepository? = null
    
    private var _loadTimerUseCase: LoadTimerUseCase? = null
    private var _startWorkoutUseCase: StartWorkoutUseCase? = null
    private var _stopWorkoutUseCase: StopWorkoutUseCase? = null
    private var _pauseWorkoutUseCase: PauseWorkoutUseCase? = null
    private var _resumeWorkoutUseCase: ResumeWorkoutUseCase? = null
    
    /**
     * Инициализация контейнера
     */
    fun initialize(context: Context) {
        _timerRepository = TimerRepository(ApiFactory.create())
        _workoutServiceRepository = WorkoutServiceRepositoryImpl()
    }
    
    fun setWorkoutService(service: TimerForegroundService) {
        (_workoutServiceRepository as? WorkoutServiceRepositoryImpl)?.setService(service)
    }
    
    // Lazy initialization of use cases
    fun getLoadTimerUseCase(): LoadTimerUseCase {
        return _loadTimerUseCase ?: LoadTimerUseCase(_timerRepository ?: throw IllegalStateException("Repository not initialized")).also {
            _loadTimerUseCase = it
        }
    }
    
    fun getStartWorkoutUseCase(): StartWorkoutUseCase {
        return _startWorkoutUseCase ?: StartWorkoutUseCase(_workoutServiceRepository ?: throw IllegalStateException("WorkoutServiceRepository not initialized")).also {
            _startWorkoutUseCase = it
        }
    }
    
    fun getStopWorkoutUseCase(): StopWorkoutUseCase {
        return _stopWorkoutUseCase ?: StopWorkoutUseCase(_workoutServiceRepository ?: throw IllegalStateException("WorkoutServiceRepository not initialized")).also {
            _stopWorkoutUseCase = it
        }
    }
    
    fun getPauseWorkoutUseCase(): PauseWorkoutUseCase {
        return _pauseWorkoutUseCase ?: PauseWorkoutUseCase(_workoutServiceRepository ?: throw IllegalStateException("WorkoutServiceRepository not initialized")).also {
            _pauseWorkoutUseCase = it
        }
    }
    
    fun getResumeWorkoutUseCase(): ResumeWorkoutUseCase {
        return _resumeWorkoutUseCase ?: ResumeWorkoutUseCase(_workoutServiceRepository ?: throw IllegalStateException("WorkoutServiceRepository not initialized")).also {
            _resumeWorkoutUseCase = it
        }
    }
}
