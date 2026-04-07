# Clean Architecture для IntervalTimer

## Обзор архитектуры

Проект реструктурирован в соответствии с **Clean Architecture** для улучшения:
- **Тестируемости** - каждый слой может быть протестирован отдельно
- **Чистоты кода** - четкое разделение ответственности
- **Расширяемости** - легко добавлять новые функции без изменения существующего кода

## Структура проекта

```
app/src/main/java/vyacheslav/pogudin/intervaltimer/
├── domain/                          # Бизнес-логика (независима от фреймворков)
│   ├── model/                       # Domain модели (Timer, Interval)
│   ├── mapper/                      # Маппинг между слоями
│   └── usecases/                    # Юзкейсы - основная бизнес-логика
│       ├── LoadTimerUseCase.kt      # Загрузка таймера по ID
│       ├── StartWorkoutUseCase.kt   # Запуск тренировки
│       ├── StopWorkoutUseCase.kt    # Остановка тренировки
│       ├── PauseWorkoutUseCase.kt   # Пауза тренировки
│       └── ResumeWorkoutUseCase.kt  # Возобновление тренировки
│
├── data/                            # Работа с данными
│   ├── api/                         # Retrofit API
│   ├── dto/                         # Data Transfer Objects
│   ├── repository/                  # Репозитории для работы с данными
│   └── service/                     # Абстракция для управления сервисом
│       ├── WorkoutServiceRepository # Интерфейс
│       └── WorkoutServiceRepositoryImpl # Реализация
│
├── ui/                              # Presentation слой (Compose)
│   ├── load/                        # Экран загрузки таймера
│   │   ├── LoadScreen.kt            # UI компонент
│   │   └── LoadViewModel.kt         # Бизнес-логика экрана
│   ├── workout/                     # Экран тренировки
│   │   ├── WorkoutScreen.kt         # UI компонент
│   │   └── WorkoutViewModel.kt      # Бизнес-логика экрана
│   ├── components/                  # Переиспользуемые компоненты
│   └── theme/                       # Темы и стили
│
├── service/                         # Android сервис
│   └── TimerForegroundService.kt    # Foreground сервис для таймера
│
├── timer/                           # Логика работы таймера
│   ├── TimerEngine.kt               # Низкоуровневый таймер
│   └── TimerState.kt                # Состояние таймера
│
├── di/                              # Dependency Injection
│   ├── AppContainer.kt              # Service Locator для управления зависимостями
│   ├── LoadViewModelFactory.kt      # Factory для LoadViewModel
│   └── WorkoutViewModelFactory.kt   # Factory для WorkoutViewModel
│
└── MainActivity.kt                  # Entry point приложения
```

## Слои архитектуры

### 1. **Domain Layer** (Независима от фреймворков)

Содержит чистую бизнес-логику приложения:

```kotlin
// domain/usecases/LoadTimerUseCase.kt
class LoadTimerUseCase(private val repository: TimerRepository) {
    suspend operator fun invoke(timerId: Int): Timer {
        if (timerId <= 0) {
            throw IllegalArgumentException("Timer ID must be positive")
        }
        return repository.getTimer(timerId)
    }
}
```

**Юзкейсы:**
- `LoadTimerUseCase` - загрузить таймер по ID с валидацией
- `StartWorkoutUseCase` - инициализировать и запустить тренировку
- `StopWorkoutUseCase` - остановить и сбросить тренировку
- `PauseWorkoutUseCase` - поставить тренировку на паузу
- `ResumeWorkoutUseCase` - возобновить тренировку

### 2. **Data Layer** (Работа с данными)

Предоставляет данные для доменного слоя через репозитории:

```kotlin
// data/service/WorkoutServiceRepository.kt
interface WorkoutServiceRepository {
    fun setTimer(timer: Timer)
    fun startTimer()
    fun pauseTimer()
    fun resumeTimer()
    fun resetTimer()
    fun stopService()
    fun getTimerState(): StateFlow<TimerState>
}

// data/service/WorkoutServiceRepositoryImpl.kt
class WorkoutServiceRepositoryImpl(private val service: TimerForegroundService?) 
    : WorkoutServiceRepository {
    // Реализация...
}
```

**Источники данных:**
- `TimerRepository` - получение таймеров через API
- `WorkoutServiceRepository` - управление состоянием сервиса тренировки

### 3. **Presentation Layer** (UI)

Использует ViewModel для управления состоянием UI и взаимодействия с юзкейсами:

```kotlin
// ui/load/LoadViewModel.kt
class LoadViewModel(private val loadTimerUseCase: LoadTimerUseCase) : ViewModel() {
    fun load() {
        viewModelScope.launch {
            try {
                timer = loadTimerUseCase(id.toInt())
            } catch (e: Exception) {
                // Обработка ошибок...
            }
        }
    }
}

// ui/workout/WorkoutViewModel.kt
class WorkoutViewModel(
    application: Application,
    val timer: Timer,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val stopWorkoutUseCase: StopWorkoutUseCase,
    // ...
) : AndroidViewModel(application) {
    fun start() {
        startWorkoutUseCase(timer)
    }
    
    fun pause() {
        pauseWorkoutUseCase()
    }
}
```

## Dependency Injection

Используется **Service Locator** паттерн для управления зависимостями:

```kotlin
// di/AppContainer.kt
object AppContainer {
    fun initialize(context: Context) {
        // Инициализация всех зависимостей
    }
    
    fun getLoadTimerUseCase(): LoadTimerUseCase { /* ... */ }
    fun getStartWorkoutUseCase(): StartWorkoutUseCase { /* ... */ }
    // ...
}
```

**ViewModel Factory:**
```kotlin
// di/LoadViewModelFactory.kt
class LoadViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoadViewModel(
            loadTimerUseCase = AppContainer.getLoadTimerUseCase()
        )
    }
}
```

**Использование в Activity:**
```kotlin
// MainActivity.kt
AppContainer.initialize(this)

val loadVm = ViewModelProvider(
    this@MainActivity,
    LoadViewModelFactory()
)[LoadViewModel::class.java]
```

## Поток данных

```
UI (LoadScreen)
    ↓
LoadViewModel
    ↓ (вызывает)
LoadTimerUseCase
    ↓ (использует)
TimerRepository
    ↓ (получает)
ApiService
    ↓
Network / Database
```

```
UI (WorkoutScreen)
    ↓
WorkoutViewModel
    ↓ (вызывает)
StartWorkoutUseCase / PauseWorkoutUseCase / ...
    ↓ (используют)
WorkoutServiceRepository (интерфейс)
    ↓ (реализация)
WorkoutServiceRepositoryImpl
    ↓ (управляет)
TimerForegroundService
    ↓
TimerEngine + TimerState
```

## Преимущества Clean Architecture

✅ **Независимость от фреймворков** - Domain Layer не зависит от Android, Retrofit и т.д.

✅ **Тестируемость** - можно протестировать каждый слой отдельно:
```kotlin
// Юзкейс можно протестировать без Android контекста
class LoadTimerUseCaseTest {
    @Test
    fun testLoadValidTimer() {
        val mockRepository = mock<TimerRepository>()
        val useCase = LoadTimerUseCase(mockRepository)
        // ...
    }
}
```

✅ **Чистое разделение ответственности** - каждый слой знает только о том, что ниже

✅ **Расширяемость** - легко добавлять новые функции (например, кеширование)

✅ **Переиспользуемость** - бизнес-логика (юзкейсы) может быть использована в разных UI (Android, Web)

## Миграция кода

### Было:
```kotlin
class LoadViewModel(private val repo: TimerRepository) : ViewModel() {
    fun load() {
        try {
            timer = repo.getTimer(id.toInt())  // Прямое обращение к репозиторию
        } catch (e: Exception) { /* ... */ }
    }
}
```

### Стало:
```kotlin
class LoadViewModel(private val loadTimerUseCase: LoadTimerUseCase) : ViewModel() {
    fun load() {
        try {
            timer = loadTimerUseCase(id.toInt())  // Через юзкейс
        } catch (e: Exception) { /* ... */ }
    }
}
```

## Дальнейшие улучшения

1. **Кеширование** - добавить кеширование в TimerRepository
2. **Обработка ошибок** - создать `Result<T>` типо для обработки успеха/ошибок
3. **Логирование** - добавить слой логирования
4. **Модульное тестирование** - написать unit тесты для юзкейсов
5. **Интеграционное тестирование** - протестировать взаимодействие между слоями
