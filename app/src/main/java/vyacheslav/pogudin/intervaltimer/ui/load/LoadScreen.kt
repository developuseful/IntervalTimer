package vyacheslav.pogudin.intervaltimer.ui.load

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import vyacheslav.pogudin.intervaltimer.domain.model.Timer
import vyacheslav.pogudin.intervaltimer.ui.theme.BorderDefault
import vyacheslav.pogudin.intervaltimer.ui.theme.BorderError
import vyacheslav.pogudin.intervaltimer.ui.theme.ButtonLoadState
import vyacheslav.pogudin.intervaltimer.ui.theme.CardBg
import vyacheslav.pogudin.intervaltimer.ui.theme.ErrorRed
import vyacheslav.pogudin.intervaltimer.ui.theme.GreenLite
import vyacheslav.pogudin.intervaltimer.ui.theme.GreenPrimary
import vyacheslav.pogudin.intervaltimer.ui.theme.GreenShadow
import vyacheslav.pogudin.intervaltimer.ui.theme.ScreenBg
import vyacheslav.pogudin.intervaltimer.ui.theme.TextPrimary
import vyacheslav.pogudin.intervaltimer.ui.theme.TextSecondary

@Composable
fun LoadScreen(
    vm: LoadViewModel,
    onLoaded: (Timer) -> Unit
) {
    vm.timer?.let { onLoaded(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(vertical = 130.dp),

        ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = GreenShadow.copy(alpha = 0.8f),  // цвет тени с прозрачностью
                        ambientColor = GreenShadow.copy(alpha = 0.8f)
                    )
                    .background(GreenPrimary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(33.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Заголовок
            Text(
                text = "Интервальный\nтаймер",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            // Описание
            Text(
                text = "Введите ID тренировки для загрузки программы интервалов",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )

            Spacer(Modifier.height(28.dp))

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
            ) {

                Text(
                    text = "ID тренировки",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))

                CustomOutlinedTextField(vm)


                // Ошибка
                vm.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = vm.error!!,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Medium,
                            color = ErrorRed
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Кнопка
                Button(
                    onClick = { vm.load() },
                    enabled = !vm.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = if (vm.loading) {
                        BorderStroke(1.dp, GreenLite)
                    } else {
                        null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        disabledContainerColor = ButtonLoadState.copy(alpha = 1f)
                    )
                ) {
                    if (vm.loading) {
                        CircularProgressIndicator(
                            color = GreenLite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Загрузка...",
                            color = GreenLite
                        )
                    } else {
                        Text(if (vm.error != null) "Повторить" else "Загрузить")
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Переключатель тестовой тренировки
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                        //.padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Использовать тестовую тренировку",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Switch(
                        checked = vm.useTestWorkout,
                        onCheckedChange = { vm.updateTestWorkout(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenPrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }

        }
    }
}

@Composable
fun CustomOutlinedTextField(
    vm: LoadViewModel,
    borderThickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    // Обработчик изменения фокуса
    val onFocusChange = { focused: Boolean ->
        // При получении фокуса сбрасываем ошибку
        if (focused && vm.error != null) {
            vm.clearError() // Метод в вашей ViewModel
        }
    }

    // Определяем цвет обводки
    val borderColor = when {
        vm.error != null -> BorderError
        else -> BorderDefault
    }

    OutlinedTextField(
        value = vm.id,
        onValueChange = {
            vm.id = it
            // Опционально: сбрасываем ошибку при вводе текста
            if (vm.error != null) {
                vm.clearError()
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            }
            .border(
                width = borderThickness,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = GreenShadow.copy(alpha = 0.1f),  // цвет тени с прозрачностью
                ambientColor = GreenShadow.copy(alpha = 0.1f)
            ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}