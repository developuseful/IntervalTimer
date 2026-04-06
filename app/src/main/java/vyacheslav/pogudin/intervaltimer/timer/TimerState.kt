package vyacheslav.pogudin.intervaltimer.timer



data class TimerState(
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val totalSeconds: Int = 0,
    val currentIntervalIndex: Int = 0,
    val remainingInInterval: Int = 0
)