package vyacheslav.pogudin.intervaltimer.data.dto

data class TimerDto(
    val timer_id: Int,
    val title: String,
    val total_time: Int,
    val intervals: List<IntervalDto>
)
