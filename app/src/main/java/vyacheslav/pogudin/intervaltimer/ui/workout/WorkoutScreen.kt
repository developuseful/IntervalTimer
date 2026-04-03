package vyacheslav.pogudin.intervaltimer.ui.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vyacheslav.pogudin.intervaltimer.domain.model.Interval

@Composable
fun WorkoutScreen(vm: WorkoutViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text(vm.timer.title, style = MaterialTheme.typography.headlineMedium)

        val intervals = vm.timer.intervals

        LazyColumn {
            itemsIndexed(intervals) { index, interval ->
                val isActive = isActiveInterval(vm.elapsed, intervals, index)
                val progress = progress(vm.elapsed, intervals, index)

                Column(Modifier.padding(8.dp)) {
                    Text(interval.title)
                    LinearProgressIndicator(progress = progress)
                    if (isActive) Text("ACTIVE")
                }
            }
        }

        Row {
            Button(onClick = { vm.start() }) { Text("Start") }
            Button(onClick = { vm.pause() }) { Text("Pause") }
            Button(onClick = { vm.resume() }) { Text("Resume") }
            Button(onClick = { vm.reset() }) { Text("Reset") }
        }
    }
}

fun isActiveInterval(elapsed: Int, list: List<Interval>, index: Int): Boolean {
    var acc = 0
    list.forEachIndexed { i, it ->
        val next = acc + it.time
        if (elapsed < next) return i == index
        acc = next
    }
    return false
}

fun progress(elapsed: Int, list: List<Interval>, index: Int): Float {
    var acc = 0
    list.forEachIndexed { i, it ->
        val next = acc + it.time
        if (elapsed < next) {
            return if (i == index) (elapsed - acc) / it.time.toFloat() else 0f
        }
        acc = next
    }
    return 1f
}