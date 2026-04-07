package vyacheslav.pogudin.intervaltimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import vyacheslav.pogudin.intervaltimer.di.AppContainer
import vyacheslav.pogudin.intervaltimer.di.LoadViewModelFactory
import vyacheslav.pogudin.intervaltimer.di.WorkoutViewModelFactory
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.ui.load.LoadScreen
import vyacheslav.pogudin.intervaltimer.ui.load.LoadViewModel
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutScreen
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = false

        super.onCreate(savedInstanceState)

        // Инициализируем DI контейнер
        AppContainer.initialize(this)

        // Запрашиваем разрешение на уведомления для Android 13+
        requestNotificationPermission()

        setContent {
            var timer by remember { mutableStateOf<Timer?>(null) }

            // Используем key(timer) чтобы пересоздать всю сцену при изменении
            key(timer) {
                if (timer == null) {
                    // ✅ Каждый раз создаем НОВЫЙ LoadViewModel
                    // Убираем remember() чтобы пересоздавать каждый раз
                    val loadVm = ViewModelProvider(
                        this@MainActivity,
                        LoadViewModelFactory()
                    )[LoadViewModel::class.java]
                    
                    // ✅ Очищаем состояние
                    loadVm.clearState()
                    
                    LoadScreen(loadVm) { selectedTimer ->
                        timer = selectedTimer
                    }
                } else {
                    val workoutVm = remember(timer) {
                        ViewModelProvider(
                            this@MainActivity,
                            WorkoutViewModelFactory(application, timer!!)
                        )[WorkoutViewModel::class.java]
                    }
                    
                    WorkoutScreen(
                        vm = workoutVm,
                        onBack = {
                            timer = null
                        },
                        onNewWorkout = {
                            timer = null
                        }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            }
        }
    }
}