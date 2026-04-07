# Clean Architecture для IntervalTimer - Краткое руководство

## 📋 Что было сделано

Проект **IntervalTimer** был рефакторен в соответствии с **Clean Architecture**. Вот что изменилось:

### ✅ Созданы юзкейсы (Domain Layer)

```
domain/usecases/
├── LoadTimerUseCase.kt        # Загрузить таймер по ID
├── StartWorkoutUseCase.kt     # Запустить тренировку  
├── StopWorkoutUseCase.kt      # Остановить тренировку
├── PauseWorkoutUseCase.kt     # Пауза тренировки
└── ResumeWorkoutUseCase.kt    # Возобновить тренировку
```

### ✅ Абстрагирован сервис (Data Layer)

```
data/service/
├── WorkoutServiceRepository.kt      # Интерфейс
└── WorkoutServiceRepositoryImpl.kt   # Реализация для TimerForegroundService
```

### ✅ Добавлена инъекция зависимостей

```
di/
├── AppContainer.kt                  # Service Locator - управление всеми зависимостями
├── LoadViewModelFactory.kt          # Factory для LoadViewModel
└── WorkoutViewModelFactory.kt       # Factory для WorkoutViewModel
```

### ✅ Обновлены ViewModels

- `LoadViewModel` - использует `LoadTimerUseCase`
- `WorkoutViewModel` - использует Start/Stop/Pause/Resume юзкейсы

### ✅ Документация

- `ARCHITECTURE.md` - полный гайд по архитектуре
- `domain/usecases/EXAMPLES.kt` - примеры использования
- этот файл - краткое руководство

---

## 🚀 Как работает сейчас

### Инициализация (MainActivity)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 1️⃣ Инициализируем контейнер зависимостей
    AppContainer.initialize(this)
    
    setContent {
        var timer by remember { mutableStateOf<Timer?>(null) }
        
        if (timer == null) {
            // 2️⃣ Создаем ViewModel через фабрику
            val loadVm = ViewModelProvider(
                this@MainActivity,
                LoadViewModelFactory()
            )[LoadViewModel::class.java]
            
            LoadScreen(loadVm) { timer = it }
        } else {
            val workoutVm = ViewModelProvider(
                this@MainActivity,
                WorkoutViewModelFactory(application, timer!!)
            )[WorkoutViewModel::class.java]
            
            WorkoutScreen(vm = workoutVm, ...)
        }
    }
}
```

### Загрузка таймера (LoadViewModel)

```kotlin
class LoadViewModel(private val loadTimerUseCase: LoadTimerUseCase) : ViewModel() {
    fun load() {
        viewModelScope.launch {
            try {
                // Вызываем юзкейс вместо прямого обращения к репозиторию
                timer = loadTimerUseCase(id.toInt())
            } catch (e: Exception) {
                // Обработка ошибок...
            }
        }
    }
}
```

### Управление тренировкой (WorkoutViewModel)

```kotlin
class WorkoutViewModel(
    application: Application,
    val timer: Timer,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val pauseWorkoutUseCase: PauseWorkoutUseCase,
    private val resumeWorkoutUseCase: ResumeWorkoutUseCase,
    private val stopWorkoutUseCase: StopWorkoutUseCase
) : AndroidViewModel(application) {
    
    fun start() {
        startWorkoutUseCase(timer)  // Юзкейс сам установит и запустит таймер
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
```

---

## 📝 Как добавить новую функцию

Например, добавить возможность **сохранения истории тренировок**:

### 1️⃣ Создать юзкейс в `domain/usecases/SaveWorkoutHistoryUseCase.kt`

```kotlin
class SaveWorkoutHistoryUseCase(private val repository: WorkoutHistoryRepository) {
    suspend operator fun invoke(history: WorkoutHistory) {
        repository.save(history)
    }
}
```

### 2️⃣ Создать интерфейс репозитория в `data/repository/WorkoutHistoryRepository.kt`

```kotlin
interface WorkoutHistoryRepository {
    suspend fun save(history: WorkoutHistory)
    suspend fun getAll(): List<WorkoutHistory>
}
```

### 3️⃣ Зарегистрировать в `di/AppContainer.kt`

```kotlin
fun getSaveWorkoutHistoryUseCase(): SaveWorkoutHistoryUseCase {
    return _saveWorkoutHistoryUseCase ?: SaveWorkoutHistoryUseCase(
        workoutHistoryRepository
    ).also { _saveWorkoutHistoryUseCase = it }
}
```

### 4️⃣ Использовать в ViewModel

```kotlin
class WorkoutViewModel(
    // ...
    private val saveWorkoutHistoryUseCase: SaveWorkoutHistoryUseCase
) : AndroidViewModel(application) {
    
    fun finishWorkout() {
        val history = WorkoutHistory(
            timerId = timer.id,
            completedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            saveWorkoutHistoryUseCase(history)
        }
    }
}
```

### 5️⃣ Обновить фабрику ViewModel

```kotlin
class WorkoutViewModelFactory(
    private val application: Application,
    private val timer: Timer
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            return WorkoutViewModel(
                application = application,
                timer = timer,
                startWorkoutUseCase = AppContainer.getStartWorkoutUseCase(),
                stopWorkoutUseCase = AppContainer.getStopWorkoutUseCase(),
                pauseWorkoutUseCase = AppContainer.getPauseWorkoutUseCase(),
                resumeWorkoutUseCase = AppContainer.getResumeWorkoutUseCase(),
                saveWorkoutHistoryUseCase = AppContainer.getSaveWorkoutHistoryUseCase()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

---

## 🧪 Тестирование юзкейсов

Теперь юзкейсы легко тестировать (не нужна Android context):

```kotlin
class LoadTimerUseCaseTest {
    
    @Test
    fun testLoadValidTimer() = runBlocking {
        // Arrange
        val mockRepository = mock<TimerRepository>()
        val expectedTimer = Timer(id = 1, title = "Test", totalTime = 60, intervals = emptyList())
        `when`(mockRepository.getTimer(1)).thenReturn(expectedTimer)
        
        val useCase = LoadTimerUseCase(mockRepository)
        
        // Act
        val result = useCase(1)
        
        // Assert
        assertEquals(expectedTimer, result)
    }
    
    @Test
    fun testThrowsWhenIdIsInvalid() {
        val mockRepository = mock<TimerRepository>()
        val useCase = LoadTimerUseCase(mockRepository)
        
        assertThrows<IllegalArgumentException> {
            runBlocking { useCase(-1) }
        }
    }
}
```

---

## 📊 Слои архитектуры

```
┌─────────────────────────────────┐
│   Presentation Layer (UI)       │
│   - LoadScreen, WorkoutScreen   │
│   - LoadViewModel, WorkoutVM    │
└────────────┬────────────────────┘
             │ (используют)
┌────────────▼────────────────────┐
│   Domain Layer (Business Logic) │
│   - UseCases                    │
│   - Models (Timer, Interval)    │
│   - Независима от фреймворков   │
└────────────┬────────────────────┘
             │ (зависят от)
┌────────────▼────────────────────┐
│   Data Layer (Data Sources)     │
│   - Repositories                │
│   - API, Database               │
└─────────────────────────────────┘
```

---

## ✨ Преимущества

✅ **Тестируемость** - юзкейсы тестируются без Android контекста  
✅ **Чистота кода** - четкое разделение ответственности  
✅ **Переиспользуемость** - бизнес-логика может быть использована в разных UI  
✅ **Расширяемость** - новые функции добавляются по известному паттерну  
✅ **Независимость** - бизнес-логика не зависит от фреймворков  

---

## 📚 Дальше читайте

- `ARCHITECTURE.md` - детальная документация по архитектуре
- `domain/usecases/EXAMPLES.kt` - примеры кода
- Исходные файлы имеют комментарии на русском

---

## 🔧 Быстрые ссылки

| Файл | Назначение |
|------|-----------|
| `domain/usecases/` | Все бизнес-логика (юзкейсы) |
| `data/repository/` | Получение данных |
| `data/service/` | Абстракция для сервиса |
| `di/AppContainer.kt` | Управление зависимостями |
| `ui/*/ViewModel.kt` | Логика экранов |
| `ui/*/Screen.kt` | UI компоненты |

Удачи с разработкой! 🚀
