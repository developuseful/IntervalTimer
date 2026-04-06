package vyacheslav.pogudin.intervaltimer.data.repository

import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

/**
 * Локальная заглушка для тестирования UI и таймера без сети.
 * Для загрузки с API установите [ENABLED] в `false`.
 */
object StubTimer {
    var ENABLED = true

    val sample: Timer = Timer(
        id = 0,
        title = "Тест: три интервала",
        totalTime = 30,
        intervals = listOf(
            Interval("Интервал 1", 5),
            Interval("Интервал 2", 10),
            Interval("Интервал 3", 15)
        )
    )
}