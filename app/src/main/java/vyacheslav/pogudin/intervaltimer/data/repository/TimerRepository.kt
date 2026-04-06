package vyacheslav.pogudin.intervaltimer.data.repository

import vyacheslav.pogudin.intervaltimer.data.repository.StubTimer
import vyacheslav.pogudin.intervaltimer.data.api.ApiService
import vyacheslav.pogudin.intervaltimer.domain.mapper.toDomain
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

class TimerRepository(private val api: ApiService) {
    suspend fun getTimer(id: Int): Timer {
        if (StubTimer.ENABLED) return StubTimer.sample
        return api.getTimer(id).timer.toDomain()
    }
}