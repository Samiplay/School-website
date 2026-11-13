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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.HubAltButton
import com.example.ui.components.HubPrimaryButton
import com.example.ui.theme.BricolageGrotesqueFamily
import com.example.ui.theme.LocalHubPalette
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    errorText: String,
    shakeCount: Int,
    onUnlockAttempt: (String) -> Unit
) {
    val palette = LocalHubPalette.current
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(shakeCount) {
        if (shakeCount > 0) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(-14f, tween(60))
            shakeOffset.animateTo(14f, tween(60))
            shakeOffset.animateTo(-8f, tween(50))
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
            shape = RoundedCornerShape(18.dp),
            color = palette.surface,
            border = BorderStroke(1.dp, palette.line),
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.lock_title),
                    fontFamily = BricolageGrotesqueFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 29.sp,
                    lineHeight = 32.sp,
                    color = palette.ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.lock_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    color = palette.muted
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lock_password_input"),
                        placeholder = {
                            Text(stringResource(R.string.lock_password_hint), color = palette.muted)
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (password.isNotEmpty()) {
                                    onUnlockAttempt(password)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.bg,
                            unfocusedContainerColor = palette.bg,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.line,
                            focusedTextColor = palette.ink,
                            unfocusedTextColor = palette.ink,
                            cursorColor = palette.accent
                        )
                    )
                    HubAltButton(
                        text = if (showPassword) stringResource(R.string.lock_hide) else stringResource(R.string.lock_show),
                        onClick = { showPassword = !showPassword },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 56.dp)
                            .testTag("lock_toggle_button")
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorText.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.spend,
                    modifier = Modifier.testTag("lock_error_text")
                )
                Spacer(Modifier.height(8.dp))
                HubPrimaryButton(
                    text = stringResource(R.string.lock_unlock),
                    onClick = {
                        if (password.isNotEmpty()) {
                            onUnlockAttempt(password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lock_unlock_button")
                )
            }
        }
    }
}
