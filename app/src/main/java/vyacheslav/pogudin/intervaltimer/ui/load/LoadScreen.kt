package vyacheslav.pogudin.intervaltimer.ui.load

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

@Composable
fun LoadScreen(vm: LoadViewModel, onLoaded: (Timer) -> Unit) {
    vm.timer?.let { onLoaded(it) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = vm.id, onValueChange = { vm.id = it })
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.load() }, enabled = !vm.loading) {
            if (vm.loading) CircularProgressIndicator() else Text("Загрузить")
        }
        vm.error?.let { Text(it) }
    }
}