package vyacheslav.pogudin.intervaltimer.domain.model

data class Timer(
    val id: Int,
    val title: String,
    val totalTime: Int,
    val intervals: List<Interval>
)