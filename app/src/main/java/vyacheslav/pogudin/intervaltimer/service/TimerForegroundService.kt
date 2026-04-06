package vyacheslav.pogudin.intervaltimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vyacheslav.pogudin.intervaltimer.MainActivity
import vyacheslav.pogudin.intervaltimer.R
import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.timer.TimerEngine
import vyacheslav.pogudin.intervaltimer.timer.TimerState
import vyacheslav.pogudin.intervaltimer.util.SoundManager
import kotlin.math.min

enum class WorkoutPhase {
    Ready, Running, Paused, Finished
}

class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerEngine = TimerEngine()
    private var currentTimer: Timer? = null
    private var updateJob: Job? = null
    private var soundManager: SoundManager? = null

    private var previousPhase = WorkoutPhase.Ready
    private var previousIntervalIndex = -1

    // Флаг для отслеживания, был ли уже показан первый интервал
    private var hasFirstIntervalStarted = false

    // Флаг, показывающее, что уведомление уже показано
    private var isNotificationShown = false

    // Текущее название интервала для отображения в уведомлении
    private var currentIntervalTitle = ""

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CLOSE = "ACTION_CLOSE"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_START -> startTimer()
                ACTION_PAUSE -> pauseTimer()
                ACTION_RESUME -> resumeTimer()
                ACTION_RESET -> resetTimer()
                ACTION_STOP -> stopSelf()
                ACTION_CLOSE -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        soundManager = SoundManager(this)

        val filter = IntentFilter().apply {
            addAction(ACTION_START)
            addAction(ACTION_PAUSE)
            addAction(ACTION_RESUME)
            addAction(ACTION_RESET)
            addAction(ACTION_STOP)
            addAction(ACTION_CLOSE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_RESET -> resetTimer()
            ACTION_STOP -> stopSelf()
            ACTION_CLOSE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun setTimer(timer: Timer) {
        currentTimer = timer
        timerEngine.reset()
        currentIntervalTitle = timer.intervals.firstOrNull()?.title ?: ""
        _timerState.value = TimerState(
            elapsedSeconds = 0,
            isRunning = false,
            totalSeconds = timer.totalTime,
            currentIntervalIndex = 0,
            remainingInInterval = timer.intervals.firstOrNull()?.time ?: 0
        )
        previousPhase = WorkoutPhase.Ready
        previousIntervalIndex = -1
        hasFirstIntervalStarted = false
        isNotificationShown = false
    }

    fun startTimer() {
        if (currentTimer == null) return

        timerEngine.start()
        startUpdateLoop()
        _timerState.value = _timerState.value.copy(isRunning = true)

        // Воспроизводим только звук старта
        soundManager?.playStart()

        // Показываем уведомление о старте с названием первого интервала
        showNotification()
    }

    fun pauseTimer() {
        timerEngine.pause()
        stopUpdateLoop()
        _timerState.value = _timerState.value.copy(isRunning = false)
        updateNotificationContent()
    }

    fun resumeTimer() {
        timerEngine.resume()
        startUpdateLoop()
        _timerState.value = _timerState.value.copy(isRunning = true)
        updateNotificationContent()
    }

    fun resetTimer() {
        timerEngine.reset()
        currentIntervalTitle = currentTimer?.intervals?.firstOrNull()?.title ?: ""
        _timerState.value = _timerState.value.copy(
            elapsedSeconds = 0,
            isRunning = false
        )
        previousIntervalIndex = -1
        hasFirstIntervalStarted = false
        hideNotification()
        isNotificationShown = false
    }

    private fun startUpdateLoop() {
        stopUpdateLoop()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(100)
                updateTimerState()
            }
        }
    }

    private fun stopUpdateLoop() {
        updateJob?.cancel()
        updateJob = null
    }

    private fun updateTimerState() {
        val timer = currentTimer ?: return
        val rawSeconds = (timerEngine.elapsed() / 1000).toInt()
        val elapsed = min(rawSeconds, timer.totalTime)

        val currentState = _timerState.value
        if (currentState.elapsedSeconds != elapsed) {
            _timerState.value = currentState.copy(elapsedSeconds = elapsed)

            // Проверяем завершение
            if (timer.totalTime > 0 && rawSeconds >= timer.totalTime && currentState.isRunning) {
                timerEngine.pause()
                stopUpdateLoop()
                _timerState.value = _timerState.value.copy(isRunning = false)
                soundManager?.playEnd()
                showCompletionNotification()
            } else {
                checkIntervalChange()
            }
        }
    }

    private fun checkIntervalChange() {
        val timer = currentTimer ?: return
        val elapsed = _timerState.value.elapsedSeconds

        val currentPhase = when {
            timer.totalTime > 0 && elapsed >= timer.totalTime -> WorkoutPhase.Finished
            _timerState.value.isRunning -> WorkoutPhase.Running
            elapsed > 0 -> WorkoutPhase.Paused
            else -> WorkoutPhase.Ready
        }

        val currentIntervalIndex = getCurrentIntervalIndex(elapsed, timer.intervals, timer.totalTime)
        val newIntervalTitle = timer.intervals.getOrNull(currentIntervalIndex)?.title ?: ""

        // Обновляем название интервала, если оно изменилось
        if (currentIntervalTitle != newIntervalTitle) {
            currentIntervalTitle = newIntervalTitle
            // Обновляем уведомление с новым названием интервала
            if (isNotificationShown) {
                updateNotificationContent()
            }
        }

        // Проверяем смену интервала для звука и отдельного уведомления
        if (currentPhase == WorkoutPhase.Running &&
            hasFirstIntervalStarted &&
            currentIntervalIndex != previousIntervalIndex &&
            currentIntervalIndex != -1 &&
            currentIntervalIndex < timer.intervals.size) {

            soundManager?.playInterval()
            // Показываем уведомление о смене интервала с новым названием
            showIntervalNotification(currentIntervalIndex + 1, timer.intervals.size, currentIntervalTitle)
        }

        // Отмечаем, что первый интервал начался
        if (!hasFirstIntervalStarted && currentPhase == WorkoutPhase.Running && elapsed > 0) {
            hasFirstIntervalStarted = true
        }

        previousPhase = currentPhase
        previousIntervalIndex = currentIntervalIndex

        // Обновляем оставшееся время в текущем интервале
        val remaining = getRemainingInInterval(elapsed, timer.intervals, timer.totalTime)
        _timerState.value = _timerState.value.copy(
            currentIntervalIndex = currentIntervalIndex,
            remainingInInterval = remaining
        )
    }

    private fun getCurrentIntervalIndex(elapsed: Int, intervals: List<Interval>, totalTime: Int): Int {
        if (intervals.isEmpty()) return 0
        val capped = elapsed.coerceAtMost(totalTime)
        var acc = 0
        intervals.forEachIndexed { i, interval ->
            val next = acc + interval.time
            if (capped < next) return i
            acc = next
        }
        return intervals.lastIndex
    }

    private fun getRemainingInInterval(elapsed: Int, intervals: List<Interval>, totalTime: Int): Int {
        if (intervals.isEmpty()) return 0
        val idx = getCurrentIntervalIndex(elapsed, intervals, totalTime)
        if (idx !in intervals.indices) return 0

        val capped = elapsed.coerceAtMost(totalTime)
        var acc = 0
        for (i in intervals.indices) {
            val interval = intervals[i]
            val start = acc
            val end = acc + interval.time
            if (i == idx) {
                return when {
                    capped <= start -> interval.time
                    capped < end -> (end - capped).coerceAtLeast(0)
                    else -> 0
                }
            }
            acc = end
        }
        return 0
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }

    private fun showNotification() {
        val timer = currentTimer ?: return
        isNotificationShown = true

        val notification = buildNotification(
            title = timer.title,
            intervalTitle = currentIntervalTitle,
            text = "Тренировка началась 🏃‍♂️",
            isOngoing = true
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showIntervalNotification(currentInterval: Int, totalIntervals: Int, intervalTitle: String) {
        val timer = currentTimer ?: return
        isNotificationShown = true

        val notification = buildNotification(
            title = timer.title,
            intervalTitle = intervalTitle,
            text = "Интервал $currentInterval из $totalIntervals",
            isOngoing = true
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification() {
        val timer = currentTimer ?: return

        val notification = buildNotification(
            title = timer.title,
            intervalTitle = "",
            text = "Тренировка завершена! Отличная работа! 🎉",
            isOngoing = false
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotificationContent() {
        if (!isNotificationShown) return

        val timer = currentTimer ?: return
        val state = _timerState.value
        val remainingSeconds = timer.totalTime - state.elapsedSeconds
        val currentIntervalNum = state.currentIntervalIndex + 1
        val totalIntervals = timer.intervals.size

        val text = if (state.isRunning) {
            "Интервал $currentIntervalNum из $totalIntervals • Осталось: ${formatTime(remainingSeconds)}"
        } else {
            "На паузе • Интервал $currentIntervalNum из $totalIntervals • Осталось: ${formatTime(remainingSeconds)}"
        }

        val notification = buildNotification(
            title = timer.title,
            intervalTitle = currentIntervalTitle,
            text = text,
            isOngoing = true
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun hideNotification() {
        if (isNotificationShown) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isNotificationShown = false
        }
    }

    private fun buildNotification(
        title: String,
        intervalTitle: String,
        text: String,
        isOngoing: Boolean
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Формируем заголовок уведомления: "Название тренировки - Название интервала"
        val contentTitle = if (intervalTitle.isNotEmpty()) {
            "$title - $intervalTitle"
        } else {
            title
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(!isOngoing)
            .setSilent(false)

        if (isOngoing) {
            builder.setOngoing(true)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    0,
                    "Закрыть",
                    getPendingIntent(ACTION_CLOSE)
                ).build()
            )
        }

        return builder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления тренировки",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о смене интервалов и завершении тренировки"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdateLoop()
        soundManager?.release()
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
        }
    }
}