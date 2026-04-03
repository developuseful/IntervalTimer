package vyacheslav.pogudin.intervaltimer.domain.mapper

import vyacheslav.pogudin.intervaltimer.data.dto.TimerDto
import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.domain.model.Timer

fun TimerDto.toDomain() = Timer(
    id = timer_id,
    title = title,
    totalTime = total_time,
    intervals = intervals.map { Interval(it.title, it.time) }
)