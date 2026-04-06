package vyacheslav.pogudin.intervaltimer.data.repository

import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

/**
 * Локальная заглушка для тестирования UI и таймера без сети.
 * Для загрузки с API установите [ENABLED] в `false`.
 */
object StubTimer {
    const val ENABLED = false

    val sample: Timer = Timer(
        id = 0,
        title = "Тест: два интервала по 15 с",
        totalTime = 30,
        intervals = listOf(
            Interval("Интервал 1", 15),
            Interval("Интервал 2", 15)
        )
    )
}