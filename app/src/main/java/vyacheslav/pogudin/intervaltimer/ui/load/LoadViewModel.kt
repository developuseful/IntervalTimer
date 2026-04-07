package vyacheslav.pogudin.intervaltimer.ui.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import vyacheslav.pogudin.intervaltimer.data.repository.StubTimer
import vyacheslav.pogudin.intervaltimer.data.repository.TimerRepository
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.io.IOException

class LoadViewModel(private val repo: TimerRepository) : ViewModel() {
    var id by mutableStateOf("68")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var timer by mutableStateOf<Timer?>(null)
    var useTestWorkout by mutableStateOf(StubTimer.ENABLED)

    fun load() {
        viewModelScope.launch {
            loading = true
            delay(1000)
            error = null
            try {
                timer = repo.getTimer(id.toInt())
            } catch (e: SocketTimeoutException) {
                error = "Сервер не отвечает. Проверьте подключение или повторите попытку."
            } catch (e: ConnectException) {
                error = "Ошибка подключения. Проверьте сеть и адрес сервера."
            } catch (e: HttpException) {
                error = when (e.code()) {
                    404 -> "Тренировка не найдена. Проверьте ID."
                    500, 502, 503 -> "Сервер недоступен. Повторите попытку позже."
                    else -> "Ошибка сервера (${e.code()}). Повторите попытку."
                }
            } catch (e: IOException) {
                error = "Ошибка сети. Проверьте подключение к интернету."
            } catch (e: NumberFormatException) {
                error = "ID должен быть числом."
            } catch (e: Exception) {
                error = "Неизвестная ошибка: ${e.localizedMessage ?: "попробуйте позже"}"
            } finally {
                loading = false
            }
        }
    }

    fun clearError() {
        error = null
    }

    fun updateTestWorkout(enabled: Boolean) {
        useTestWorkout = enabled
        StubTimer.ENABLED = enabled
    }
}
