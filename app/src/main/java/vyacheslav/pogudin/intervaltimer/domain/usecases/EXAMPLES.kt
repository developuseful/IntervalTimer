package vyacheslav.pogudin.intervaltimer.domain.usecases

/**
 * Файл с примерами использования юзкейсов в приложении
 * 
 * ПРИМЕР 1: Загрузка таймера в LoadViewModel
 * ============================================
 */
object LoadTimerUseCaseExample {
    /*
    class LoadViewModel(private val loadTimerUseCase: LoadTimerUseCase) : ViewModel() {
        var timer by mutableStateOf<Timer?>(null)
        var errorResId by mutableStateOf<Int?>(null)
        
        fun load() {
            viewModelScope.launch {
                try {
                    // Простой вызов юзкейса
                    timer = loadTimerUseCase(id.toInt())
                } catch (e: IllegalArgumentException) {
                    errorResId = R.string.error_invalid_id
                } catch (e: HttpException) {
                    errorResId = when (e.code()) {
                        404 -> R.string.error_not_found
                        else -> R.string.error_server
                    }
                } catch (e: IOException) {
                    errorResId = R.string.error_network
                }
            }
        }
    }
    */
}

/**
 * ПРИМЕР 2: Запуск тренировки в WorkoutViewModel
 * ===============================================
 */
object StartWorkoutUseCaseExample {
    /*
    class WorkoutViewModel(
        application: Application,
        val timer: Timer,
        private val startWorkoutUseCase: StartWorkoutUseCase,
        private val pauseWorkoutUseCase: PauseWorkoutUseCase,
        private val resumeWorkoutUseCase: ResumeWorkoutUseCase,
        private val stopWorkoutUseCase: StopWorkoutUseCase
    ) : AndroidViewModel(application) {
        
        fun start() {
            // Запуск тренировки через юзкейс
            // Юзкейс сам установит таймер и запустит его
            startWorkoutUseCase(timer)
        }
        
        fun pause() {
            pauseWorkoutUseCase()
        }
        
        fun resume() {
            resumeWorkoutUseCase()
        }
        
        fun stop() {
            stopWorkoutUseCase()
        }
    }
    */
}

/**
 * ПРИМЕР 3: Создание ViewModels с DI в MainActivity
 * ==================================================
 */
object DIExample {
    /*
    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // Инициализируем DI контейнер один раз
            AppContainer.initialize(this)
            
            setContent {
                var timer by remember { mutableStateOf<Timer?>(null) }
                
                key(timer) {
                    if (timer == null) {
                        // Создаем LoadViewModel с фабрикой
                        val loadVm = ViewModelProvider(
                            this@MainActivity,
                            LoadViewModelFactory()
                        )[LoadViewModel::class.java]
                        
                        LoadScreen(loadVm) { timer = it }
                    } else {
                        // Создаем WorkoutViewModel с фабрикой
                        val workoutVm = ViewModelProvider(
                            this@MainActivity,
                            WorkoutViewModelFactory(application, timer!!)
                        )[WorkoutViewModel::class.java]
                        
                        WorkoutScreen(vm = workoutVm, ...)
                    }
                }
            }
        }
    }
    */
}

/**
 * ПРИМЕР 4: Unit тест для юзкейса
 * ================================
 */
object UseCaseTestExample {
    /*
    import org.junit.Test
    import org.mockito.Mockito.mock
    import org.mockito.Mockito.`when`
    import kotlinx.coroutines.runBlocking
    
    class LoadTimerUseCaseTest {
        
        @Test
        fun loadValidTimer() = runBlocking {
            // Arrange
            val mockRepository = mock<TimerRepository>()
            val expectedTimer = Timer(id = 1, title = "Test", totalTime = 60, intervals = emptyList())
            `when`(mockRepository.getTimer(1)).thenReturn(expectedTimer)
            
            val useCase = LoadTimerUseCase(mockRepository)
            
            // Act
            val result = useCase(1)
            
            // Assert
            assert(result == expectedTimer)
        }
        
        @Test
        fun throwsWhenIdIsInvalid() {
            // Arrange
            val mockRepository = mock<TimerRepository>()
            val useCase = LoadTimerUseCase(mockRepository)
            
            // Act & Assert
            try {
                runBlocking {
                    useCase(-1)
                }
                assert(false) { "Should have thrown exception" }
            } catch (e: IllegalArgumentException) {
                assert(e.message == "Timer ID must be positive")
            }
        }
    }
    */
}

/**
 * ДОБАВЛЕНИЕ НОВОЙ ФУНКЦИИ: Пошаговая инструкция
 * ===============================================
 * 
 * Пример: добавить юзкейс для получения всех загруженных таймеров
 * 
 * 1. Создать юзкейс в domain/usecases/:
 *    class GetAllTimersUseCase(private val repository: TimerRepository) {
 *        suspend operator fun invoke(): List<Timer> {
 *            return repository.getAllTimers()
 *        }
 *    }
 * 
 * 2. Добавить метод в repository (data/repository/):
 *    suspend fun getAllTimers(): List<Timer> {
 *        return api.getAllTimers().timers.map { it.toDomain() }
 *    }
 * 
 * 3. Добавить метод в API (data/api/):
 *    @GET("/timers")
 *    suspend fun getAllTimers(): TimersResponse
 * 
 * 4. Зарегистрировать юзкейс в AppContainer (di/):
 *    fun getGetAllTimersUseCase(): GetAllTimersUseCase {
 *        return _getAllTimersUseCase ?: GetAllTimersUseCase(timerRepository).also {
 *            _getAllTimersUseCase = it
 *        }
 *    }
 * 
 * 5. Использовать в ViewModel:
 *    class MyViewModel(private val getAllTimersUseCase: GetAllTimersUseCase) {
 *        fun loadAllTimers() {
 *            viewModelScope.launch {
 *                val timers = getAllTimersUseCase()
 *                // обновить UI
 *            }
 *        }
 *    }
 * 
 * 6. Создать factory для ViewModel (di/):
 *    class MyViewModelFactory : ViewModelProvider.Factory {
 *        override fun <T : ViewModel> create(modelClass: Class<T>): T {
 *            return MyViewModel(AppContainer.getGetAllTimersUseCase())
 *        }
 *    }
 * 
 * Готово! Новая функция добавлена в соответствии с Clean Architecture.
 */
