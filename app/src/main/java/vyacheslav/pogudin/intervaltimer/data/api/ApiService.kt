package vyacheslav.pogudin.intervaltimer.data.api

import vyacheslav.pogudin.intervaltimer.data.dto.TimerResponse

interface ApiService {
    @retrofit2.http.GET("api/interval-timers/{id}")
    suspend fun getTimer(@retrofit2.http.Path("id") id: Int): TimerResponse
}