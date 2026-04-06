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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import vyacheslav.pogudin.intervaltimer.data.api.ApiFactory
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
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

        // Запрашиваем разрешение на уведомления для Android 13+
        requestNotificationPermission()

        val repo = TimerRepository(ApiFactory.create())
        val loadVm = LoadViewModel(repo)

        setContent {
            var timer by remember { mutableStateOf<Timer?>(null) }

            if (timer == null) {
                LoadScreen(loadVm) { timer = it }
            } else {
                val workoutVm = remember(timer) {
                    WorkoutViewModel(application, timer!!)
                }
                WorkoutScreen(
                    vm = workoutVm,
                    onBack = {
                        workoutVm.stopAndUnbind()
                        timer = null
                    },
                    onNewWorkout = {
                        workoutVm.stopAndUnbind()
                        timer = null
                    }
                )
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