package vyacheslav.pogudin.intervaltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import vyacheslav.pogudin.intervaltimer.data.api.ApiFactory
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.ui.load.LoadScreen
import vyacheslav.pogudin.intervaltimer.ui.load.LoadViewModel
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutScreen
import vyacheslav.pogudin.intervaltimer.ui.workout.WorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = TimerRepository(ApiFactory.create())
        val loadVm = LoadViewModel(repo)

        setContent {
            var timer by remember { mutableStateOf<Timer?>(null) }

            if (timer == null) {
                LoadScreen(loadVm) { timer = it }
            } else {
                WorkoutScreen(WorkoutViewModel(timer!!))
            }
        }
    }
}