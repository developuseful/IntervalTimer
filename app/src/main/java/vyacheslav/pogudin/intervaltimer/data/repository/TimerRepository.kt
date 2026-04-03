package vyacheslav.pogudin.intervaltimer.data.repository

import vyacheslav.pogudin.intervaltimer.data.api.ApiService

class TimerRepository(private val api: ApiService) {
    suspend fun getTimer(id: Int): Timer {
        return api.getTimer(id).timer.toDomain()
    }
}