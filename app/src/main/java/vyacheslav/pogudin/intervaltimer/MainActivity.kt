package vyacheslav.pogudin.intervaltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import vyacheslav.pogudin.intervaltimer.data.api.ApiFactory
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.ui.load.LoadScreen
import vyacheslav.pogudin.intervaltimer.ui.load.LoadViewModel
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutScreen
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // Отключаем автоматическое применение системных стилей
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Получаем контроллер для управления панелями
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Только статус-бар: темные иконки (для светлого фона)
        controller.isAppearanceLightStatusBars = true

        // Навигационная панель: оставляем как есть (светлые иконки на темном фоне)
        controller.isAppearanceLightNavigationBars = false

        super.onCreate(savedInstanceState)

        val repo = TimerRepository(ApiFactory.create())
        val loadVm = LoadViewModel(repo)

        setContent {
            var timer by remember { mutableStateOf<Timer?>(null) }

            if (timer == null) {
                LoadScreen(loadVm) { timer = it }
            } else {
                val workoutVm = remember(timer) { WorkoutViewModel(timer!!) }
                WorkoutScreen(
                    vm = workoutVm,
                    onBack = { timer = null },
                    onNewWorkout = { timer = null }
                )
            }
        }
    }
}