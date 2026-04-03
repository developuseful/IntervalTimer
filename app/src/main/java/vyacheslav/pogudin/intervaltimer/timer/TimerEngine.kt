package vyacheslav.pogudin.intervaltimer.timer

class TimerEngine {
    private var startTime = 0L
    private var paused = 0L
    private var running = false

    fun start() {
        startTime = System.currentTimeMillis()
        running = true
    }

    fun pause() {
        paused = elapsed()
        running = false
    }

    fun resume() {
        startTime = System.currentTimeMillis() - paused
        running = true
    }

    fun reset() {
        startTime = 0
        paused = 0
        running = false
    }

    fun elapsed(): Long {
        return if (running) System.currentTimeMillis() - startTime else paused
    }
}