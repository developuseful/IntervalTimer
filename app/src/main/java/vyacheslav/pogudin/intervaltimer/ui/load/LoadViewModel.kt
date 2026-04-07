package vyacheslav.pogudin.intervaltimer.ui.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vyacheslav.pogudin.intervaltimer.R
import vyacheslav.pogudin.intervaltimer.data.repository.StubTimer
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class LoadViewModel(private val repo: TimerRepository) : ViewModel() {
    var id by mutableStateOf("68")
    var loading by mutableStateOf(false)
    var errorResId by mutableStateOf<Int?>(null)
    var errorDetails by mutableStateOf<String?>(null)
    var timer by mutableStateOf<Timer?>(null)
    var useTestWorkout by mutableStateOf(StubTimer.ENABLED)

    fun load() {
        viewModelScope.launch {
            loading = true
            delay(1000)
            errorResId = null
            errorDetails = null
            try {
                timer = repo.getTimer(id.toInt())
            } catch (e: SocketTimeoutException) {
                errorResId = R.string.error_server_timeout
            } catch (e: ConnectException) {
                errorResId = R.string.error_connect
            } catch (e: HttpException) {
                errorResId = when (e.code()) {
                    404 -> R.string.error_not_found
                    500, 502, 503 -> R.string.error_server_unavailable
                    else -> R.string.error_server
                }
                if (errorResId == R.string.error_server) {
                    errorDetails = e.code().toString()
                }
            } catch (e: IOException) {
                errorResId = R.string.error_network
            } catch (e: NumberFormatException) {
                errorResId = R.string.error_invalid_id
            } catch (e: Exception) {
                errorResId = R.string.error_unknown
                errorDetails = e.localizedMessage
            } finally {
                loading = false
            }
        }
    }

    fun clearError() {
        errorResId = null
        errorDetails = null
    }

    fun updateTestWorkout(enabled: Boolean) {
        useTestWorkout = enabled
        StubTimer.ENABLED = enabled
    }
}
