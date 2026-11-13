package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalHubColors
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    errorMessage: String?,
    shakeCounter: Int,
    onAttemptUnlock: (String) -> Unit,
    onPasswordChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hubColors = LocalHubColors.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(shakeCounter) {
        if (shakeCounter > 0) {
            val sequence = listOf(-18f, 18f, -12f, 12f, -6f, 6f, 0f)
            sequence.forEach { target ->
                shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 38))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(hubColors.bg)
            .safeDrawingPadding()
            .imePadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
            shape = RoundedCornerShape(18.dp),
            color = hubColors.surface,
            border = BorderStroke(1.dp, hubColors.line),
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.lock_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = hubColors.ink
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.lock_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = hubColors.muted
                )
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onPasswordChanged()
                        },
                        placeholder = { Text(stringResource(R.string.lock_password_hint)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onAttemptUnlock(password) }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lock_password_input")
                    )

                    OutlinedButton(
                        onClick = { passwordVisible = !passwordVisible },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, hubColors.line),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("lock_show_toggle")
                    ) {
                        Text(
                            text = if (passwordVisible) stringResource(R.string.lock_hide) else stringResource(R.string.lock_show),
                            color = hubColors.ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = hubColors.spend,
                    modifier = Modifier.height(22.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { onAttemptUnlock(password) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = hubColors.accent,
                        contentColor = hubColors.accentInk
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("unlock_button")
                ) {
                    Text(
                        text = stringResource(R.string.lock_unlock),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
