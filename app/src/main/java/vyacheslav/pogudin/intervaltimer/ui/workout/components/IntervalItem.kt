package vyacheslav.pogudin.intervaltimer.ui.workout.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vyacheslav.pogudin.intervaltimer.domain.model.Interval
import vyacheslav.pogudin.intervaltimer.service.WorkoutPhase
import vyacheslav.pogudin.intervaltimer.ui.theme.BorderDefault
import vyacheslav.pogudin.intervaltimer.ui.theme.IntervalActiveGreenTint
import vyacheslav.pogudin.intervaltimer.ui.theme.IntervalActiveOrangeTint
import vyacheslav.pogudin.intervaltimer.ui.theme.IntervalBadgeMuted
import vyacheslav.pogudin.intervaltimer.ui.theme.IntervalRowSurface
import vyacheslav.pogudin.intervaltimer.ui.theme.TextPrimary
import vyacheslav.pogudin.intervaltimer.ui.theme.TextSecondary
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateBlue
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateGreen
import vyacheslav.pogudin.intervaltimer.ui.theme.WorkoutStateOrange
import vyacheslav.pogudin.intervaltimer.ui.workout.currentIntervalIndex
import vyacheslav.pogudin.intervaltimer.ui.workout.formatMmSs

// Добавляем недостающую функцию
private fun remainingForIntervalAtIndex(
    intervalIndex: Int,
    elapsed: Int,
    intervals: List<Interval>,
    totalTime: Int
): Int {
    if (intervalIndex !in intervals.indices) return 0
    val capped = elapsed.coerceAtMost(totalTime)
    var acc = 0
    for (i in intervals.indices) {
        val interval = intervals[i]
        val start = acc
        val end = acc + interval.time
        if (i == intervalIndex) {
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

private fun intervalsCountRu(n: Int): String {
    val word = when {
        n % 100 in 11..14 -> "интервалов"
        n % 10 == 1 -> "интервал"
        n % 10 in 2..4 -> "интервала"
        else -> "интервалов"
    }
    return "$n $word"
}

@Composable
fun IntervalsSection(
    intervals: List<Interval>,
    phase: WorkoutPhase,
    elapsed: Int,
    totalTime: Int,
    modifier: Modifier = Modifier
) {
    val currentIdx = currentIntervalIndex(elapsed, intervals, totalTime)
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Интервалы",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val statusText = when (phase) {
                    WorkoutPhase.Ready -> intervalsCountRu(intervals.size)
                    WorkoutPhase.Running, WorkoutPhase.Paused ->
                        "${currentIdx + 1} из ${intervals.size}"

                    WorkoutPhase.Finished -> "${intervals.size} из ${intervals.size}"
                }
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                if (phase == WorkoutPhase.Finished) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        intervals.forEachIndexed { index, interval ->
            if (index > 0) {
                Spacer(Modifier.size(5.dp))
            }
            IntervalRow(
                index = index,
                interval = interval,
                phase = phase,
                elapsed = elapsed,
                totalTime = totalTime,
                intervals = intervals
            )
        }
    }
}

@Composable
private fun IntervalRow(
    index: Int,
    interval: Interval,
    phase: WorkoutPhase,
    elapsed: Int,
    totalTime: Int,
    intervals: List<Interval>
) {
    val currentIdx = currentIntervalIndex(elapsed, intervals, totalTime)

    // Специальная обработка для Ready состояния - первый интервал считается "текущим"
    val isReadyFirstInterval = phase == WorkoutPhase.Ready && index == 0

    val done = when (phase) {
        WorkoutPhase.Finished -> true
        WorkoutPhase.Ready -> false
        WorkoutPhase.Running, WorkoutPhase.Paused -> index < currentIdx
    }

    val current = when (phase) {
        WorkoutPhase.Ready -> isReadyFirstInterval  // Первый интервал текущий в Ready
        WorkoutPhase.Finished -> false
        WorkoutPhase.Running, WorkoutPhase.Paused -> index == currentIdx && elapsed < totalTime
    }

    // Вычисляем процент выполнения текущего интервала
    val progress = when {
        current && (phase == WorkoutPhase.Running || phase == WorkoutPhase.Paused) -> {
            val elapsedInCurrent = elapsed - intervals.take(currentIdx).sumOf { it.time }
            val currentIntervalTime = intervals[currentIdx].time
            (elapsedInCurrent.toFloat() / currentIntervalTime).coerceIn(0f, 1f)
        }
        else -> 0f
    }

    val rowBg = when {
        phase == WorkoutPhase.Ready && current -> IntervalActiveGreenTint  // Зеленый фон для Ready
        phase == WorkoutPhase.Running && current -> IntervalActiveGreenTint
        phase == WorkoutPhase.Paused && current -> IntervalActiveOrangeTint
        else -> IntervalRowSurface
    }

    // Определяем цвет border для активного интервала
    val borderColor = when {
        current && (phase == WorkoutPhase.Ready || phase == WorkoutPhase.Running) -> WorkoutStateGreen
        current && phase == WorkoutPhase.Paused -> WorkoutStateOrange
        else -> BorderDefault.copy(alpha = 0.6f)
    }

    // Толщина border
    val borderWidth = 0.3.dp

    val durationColor = when {
        phase == WorkoutPhase.Ready && current -> WorkoutStateGreen  // Зеленый текст для Ready
        phase == WorkoutPhase.Running && current -> WorkoutStateGreen
        phase == WorkoutPhase.Paused && current -> WorkoutStateOrange
        done -> TextSecondary.copy(alpha = 0.65f)
        else -> TextSecondary
    }

    val rowAlpha =
        if (done && (phase == WorkoutPhase.Running || phase == WorkoutPhase.Paused || phase == WorkoutPhase.Finished)) 0.8f
        else 1f

    val titleColor = when {
        done -> TextSecondary.copy(alpha = 0.55f)
        phase == WorkoutPhase.Ready && current -> TextPrimary
        else -> TextPrimary
    }

    val durationSeconds = when (phase) {
        WorkoutPhase.Running, WorkoutPhase.Paused ->
            if (done) interval.time
            else remainingForIntervalAtIndex(index, elapsed, intervals, totalTime)
        WorkoutPhase.Ready -> interval.time  // Показываем полную длительность в Ready
        else -> interval.time
    }

    val titleStrikeThrough = when {
        phase == WorkoutPhase.Finished -> true
        phase == WorkoutPhase.Ready -> false  // Без зачеркивания в Ready
        else -> done && (phase == WorkoutPhase.Running || phase == WorkoutPhase.Paused)
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 300)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clip(RoundedCornerShape(16.dp))
            .background(IntervalRowSurface)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .then(
                // Добавляем заполнение фона (только для Running/Paused)
                if (progress > 0f && (phase == WorkoutPhase.Running || phase == WorkoutPhase.Paused)) {
                    Modifier.drawBehind {
                        val fillWidth = size.width * animatedProgress.value
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    rowBg,
                                    rowBg.copy(alpha = 0.7f)
                                )
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(fillWidth, size.height)
                        )
                    }
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IntervalBadge(
            index = index + 1,
            phase = phase,
            done = done,
            current = current
        )
        Text(
            text = interval.title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                textDecoration = if (titleStrikeThrough) TextDecoration.LineThrough else TextDecoration.None
            )
        )
        Text(
            text = formatMmSs(durationSeconds),
            fontSize = 15.sp,
            fontWeight = if (current && phase != WorkoutPhase.Finished) FontWeight.Medium else FontWeight.Normal,
            color = durationColor
        )
    }
}

@Composable
private fun IntervalBadge(
    index: Int,
    phase: WorkoutPhase,
    done: Boolean,
    current: Boolean
) {
    val size = 32.dp

    when {
        // Завершенные интервалы во время тренировки - только галочка, без фона и границы
        done && phase != WorkoutPhase.Ready && phase != WorkoutPhase.Finished -> {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Завершенные интервалы после окончания тренировки - синий кружок с галочкой
        done && phase == WorkoutPhase.Finished -> {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = WorkoutStateBlue.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Активный интервал в состоянии Ready - зеленый кружок с номером
        current && phase == WorkoutPhase.Ready -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(WorkoutStateGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        // Активный интервал во время тренировки - цветной кружок с номером
        current && (phase == WorkoutPhase.Running || phase == WorkoutPhase.Paused) -> {
            val bg = if (phase == WorkoutPhase.Running) WorkoutStateGreen else WorkoutStateOrange
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        // Будущие интервалы - пустой кружок с номером
        else -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(IntervalBadgeMuted)
                    .border(1.dp, TextSecondary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = TextSecondary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}