package vyacheslav.pogudin.intervaltimer.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vyacheslav.pogudin.intervaltimer.R
import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.ui.theme.CardBg
import vyacheslav.pogudin.intervaltimer.ui.theme.ErrorRed
import vyacheslav.pogudin.intervaltimer.ui.theme.GreyLight
import vyacheslav.pogudin.intervaltimer.ui.theme.ScreenBg
import vyacheslav.pogudin.intervaltimer.ui.theme.TextPrimary
import vyacheslav.pogudin.intervaltimer.ui.theme.TextSecondary
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateBlue
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateGreen
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateOrange
import vyacheslav.pogudin.intervaltimer.ui.workout.components.IntervalsSection

fun formatMmSs(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

fun currentIntervalIndex(elapsed: Int, intervals: List<Interval>, totalTime: Int): Int {
    if (intervals.isEmpty()) return 0
    val capped = elapsed.coerceAtMost(totalTime)
    var acc = 0
    intervals.forEachIndexed { i, it ->
        val next = acc + it.time
        if (capped < next) return i
        acc = next
    }
    return intervals.lastIndex
}

fun remainingForIntervalAtIndex(
    intervalIndex: Int,
    elapsed: Int,
    intervals: List<Interval>,
    totalTime: Int
): Int {
    if (intervalIndex !in intervals.indices) return 0
    val capped = elapsed.coerceAtMost(totalTime)
    var acc = 0
    for (i in intervals.indices) {
        val it = intervals[i]
        val start = acc
        val end = acc + it.time
        if (i == intervalIndex) {
            return when {
                capped <= start -> it.time
                capped < end -> (end - capped).coerceAtLeast(0)
                else -> 0
            }
        }
        acc = end
    }
    return 0
}

private fun remainingInCurrentInterval(
    elapsed: Int,
    intervals: List<Interval>,
    totalTime: Int
): Int {
    if (intervals.isEmpty()) return 0
    val idx = currentIntervalIndex(elapsed, intervals, totalTime)
    return remainingForIntervalAtIndex(idx, elapsed, intervals, totalTime)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    vm: WorkoutViewModel,
    onBack: () -> Unit,
    onNewWorkout: () -> Unit = onBack
) {
    val phase = vm.phase
    val intervals = vm.timer.intervals
    val total = vm.timer.totalTime
    val elapsed = vm.elapsed

    val accent = when (phase) {
        WorkoutPhase.Ready -> TextSecondary
        WorkoutPhase.Running -> WorkoutStateGreen
        WorkoutPhase.Paused -> WorkoutStateOrange
        WorkoutPhase.Finished -> WorkoutStateBlue
    }

    val statusLabel = when (phase) {
        WorkoutPhase.Ready -> "ГОТОВО К СТАРТУ"
        WorkoutPhase.Running -> "ВЫПОЛНЯЕТСЯ"
        WorkoutPhase.Paused -> "НА ПАУЗЕ"
        WorkoutPhase.Finished -> "ТРЕНИРОВКА ЗАВЕРШЕНА"
    }

    val subtitle = when (phase) {
        WorkoutPhase.Finished -> "Отличная работа!"
        else -> {
            val idx = currentIntervalIndex(elapsed, intervals, total)
            intervals.getOrNull(idx)?.title ?: ""
        }
    }

    val mainTimerText = when (phase) {
        WorkoutPhase.Ready -> formatMmSs(total)
        WorkoutPhase.Finished -> "0:00"
        WorkoutPhase.Running, WorkoutPhase.Paused ->
            formatMmSs(remainingInCurrentInterval(elapsed, intervals, total))
    }

    val footerText = when (phase) {
        WorkoutPhase.Ready -> "Общее время"
        WorkoutPhase.Finished -> "${formatMmSs(total)} из ${formatMmSs(total)}"
        WorkoutPhase.Running, WorkoutPhase.Paused ->
            "Прошло ${formatMmSs(elapsed)} из ${formatMmSs(total)}"
    }

    val progress = if (total > 0) elapsed / total.toFloat() else 0f

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = vm.timer.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    WorkoutTopBarTrailing(phase = phase, elapsed = elapsed, total = total)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ScreenBg,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                WorkoutMainCard(
                    phase = phase,
                    accent = accent,
                    statusLabel = statusLabel,
                    subtitle = subtitle,
                    mainTimerText = mainTimerText,
                    footerText = footerText,
                    progress = progress.coerceIn(0f, 1f)
                )
                if (phase == WorkoutPhase.Finished) {
                    Spacer(Modifier.height(16.dp))
                    FinishedStatsRow(totalSeconds = total, intervalCount = intervals.size)
                }
                Spacer(Modifier.height(20.dp))
                IntervalsSection(
                    intervals = intervals,
                    phase = phase,
                    elapsed = elapsed,
                    totalTime = total
                )
                Spacer(Modifier.height(24.dp))
            }
            WorkoutBottomActions(
                phase = phase,
                onStart = { vm.start() },
                onPause = { vm.pause() },
                onResume = { vm.resume() },
                onReset = { vm.reset() },
                onNewWorkout = onNewWorkout,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun WorkoutTopBarTrailing(
    phase: WorkoutPhase,
    elapsed: Int,
    total: Int
) {
    Row(
        modifier = Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        when (phase) {
            WorkoutPhase.Ready -> {
                Text(
                    text = formatMmSs(total),
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }

            WorkoutPhase.Running -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WorkoutStateGreen)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = formatMmSs(elapsed),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = WorkoutStateGreen
                )
            }

            WorkoutPhase.Paused -> {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    tint = WorkoutStateOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "Пауза",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = WorkoutStateOrange
                )
            }

            WorkoutPhase.Finished -> {
                Text(
                    text = "Завершена",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = WorkoutStateBlue
                )
            }
        }
    }
}

@Composable
private fun WorkoutMainCard(
    phase: WorkoutPhase,
    accent: Color,
    statusLabel: String,
    subtitle: String,
    mainTimerText: String,
    footerText: String,
    progress: Float
) {
    val timerColor = when (phase) {
        WorkoutPhase.Ready -> TextPrimary
        else -> accent
    }
    val statusColor = when (phase) {
        WorkoutPhase.Ready -> TextSecondary
        else -> accent
    }
    val subtitleColor = when (phase) {
        WorkoutPhase.Finished -> statusColor
        else -> TextSecondary
    }

    // Градиент в зависимости от состояния
    val backgroundGradient = when (phase) {
        WorkoutPhase.Ready -> null // Без градиента для состояния "Готов к старту"
        WorkoutPhase.Running -> Brush.verticalGradient(
            colors = listOf(Color(0xFFE6F1E8), Color.White) // Светло-зеленый к белому
        )
        WorkoutPhase.Paused -> Brush.verticalGradient(
            colors = listOf(Color(0xFFF3ECE1), Color.White) // Светло-оранжевый к белому
        )
        WorkoutPhase.Finished -> Brush.verticalGradient(
            colors = listOf(Color(0xFFE3ECF5), Color.White) // Светло-синий к белому
        )
    }

    // Цвет обводки в зависимости от состояния
    val borderColor = when (phase) {
        WorkoutPhase.Ready -> Color.LightGray      // Светло-серый
        WorkoutPhase.Running -> WorkoutStateGreen  // Зеленый
        WorkoutPhase.Paused -> WorkoutStateOrange  // Оранжевый
        WorkoutPhase.Finished -> WorkoutStateBlue  // Синий
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = backgroundGradient ?: Brush.verticalGradient(
                        colors = listOf(Color.White, Color.White)
                    )
                )
                .then(
                    if (phase == WorkoutPhase.Ready) {
                        Modifier.background(Color.White)
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = statusColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                )
                Spacer(Modifier.height(10.dp))

                val ptmono = FontFamily(
                    Font(R.font.ptmono_regular)
                )

                Text(
                    text = mainTimerText,
                    fontSize = 68.sp,
                    fontFamily = ptmono,
                    fontWeight = FontWeight.Bold,
                    color = timerColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = footerText,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun FinishedStatsRow(totalSeconds: Int, intervalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            value = formatMmSs(totalSeconds),
            label = "Общее время",
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            value = "$intervalCount",
            label = "Интервалов",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatMiniCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .padding(vertical = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val ptmono = FontFamily(
                Font(R.font.ptmono_regular)
            )

            Text(
                text = value,
                fontSize = 20.sp,
                fontFamily = ptmono,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun WorkoutBottomActions(
    phase: WorkoutPhase,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onNewWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (phase) {
            WorkoutPhase.Ready -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutStateGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Старт", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            WorkoutPhase.Running -> {
                Button(
                    onClick = onPause,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutStateOrange)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Пауза", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(0.1.dp, ErrorRed, RoundedCornerShape(16.dp)) ,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Сбросить тренировку", color = ErrorRed, fontWeight = FontWeight.Medium)
                }
            }

            WorkoutPhase.Paused -> {
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutStateGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Продолжить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(0.1.dp, ErrorRed, RoundedCornerShape(16.dp)) ,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Сбросить тренировку", color = ErrorRed, fontWeight = FontWeight.Medium)
                }
            }

            WorkoutPhase.Finished -> {
                Button(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutStateBlue)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Запустить заново", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onNewWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(0.1.dp, GreyLight, RoundedCornerShape(16.dp)) ,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Новая тренировка", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
