package com.breakfast.designsystem

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breakfast.R


@Composable
fun BreakfastButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    isHasObserver: Boolean = false,
    firstGradient: Color = colorResource(id = R.color.blue_ribbon),
    secondGradient: Color = colorResource(id = R.color.governor_bay),
    cornerRadius: Dp = 12.dp,
    height: Dp = 48.dp,
    @DrawableRes iconRes: Int? = null,
    content: @Composable () -> Unit
) {
    val isEffectivelyEnabled = enabled && !isHasObserver
    val useLightGrey = (!isEffectivelyEnabled) || (isDanger && !enabled)
    val shape = RoundedCornerShape(cornerRadius)

    val backgroundModifier = when {
        isDanger && enabled -> modifier
            .clip(shape)
            .background(Color.Red)
        useLightGrey -> modifier
            .clip(shape)
            .background(Color.LightGray)
        else -> modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(firstGradient, secondGradient)
                )
            )
    }

    val contentColor: Color = when {
        useLightGrey -> Color.DarkGray
        isDanger && enabled -> colorResource(id = R.color.card_bg)
        else -> colorResource(id = R.color.card_bg)
    }

    Box(
        modifier = backgroundModifier
            .fillMaxWidth()
            .height(height)
            .clickable(enabled = isEffectivelyEnabled, onClick = onClick)
            .testTag("BreakfastButton"),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.ProvideTextStyle(value = androidx.compose.material3.MaterialTheme.typography.labelLarge) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            // Icon will inherit tint from LocalContentColor → same as text
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    content()
                }
            }
        }
    }
}

@Composable
fun BreakfastButtonRes(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    isHasObserver: Boolean = false,
    @ColorRes firstGradientRes: Int = R.color.blue_ribbon,
    @ColorRes secondGradientRes: Int = R.color.governor_bay,
    cornerRadius: Dp = 12.dp,
    height: Dp = 50.dp,
    @DrawableRes iconRes: Int? = null,
    content: @Composable () -> Unit
) {
    val first = colorResource(id = firstGradientRes)
    val second = colorResource(id = secondGradientRes)
    BreakfastButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isDanger = isDanger,
        isHasObserver = isHasObserver,
        firstGradient = first,
        secondGradient = second,
        cornerRadius = cornerRadius,
        height = height,
        iconRes = iconRes,
        content = content
    )
}

@Preview(showBackground = true, name = "BreakfastButton States")
@Composable
private fun BreakfastButtonPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        BreakfastButtonRes(onClick = { /* no-op */ }) {
            Text("Gradient Enabled")
        }
        Spacer(modifier = Modifier.height(8.dp))
        BreakfastButtonRes(onClick = { }, enabled = false) {
            Text("Disabled / Light Grey")
        }
        Spacer(modifier = Modifier.height(8.dp))
        BreakfastButtonRes(onClick = { }, isHasObserver = true) {
            Text("Has Observer / Light Grey")
        }
        Spacer(modifier = Modifier.height(8.dp))
        BreakfastButtonRes(onClick = { }, isDanger = true) {
            Text("Danger Enabled")
        }
        Spacer(modifier = Modifier.height(8.dp))
        BreakfastButtonRes(onClick = { }, isDanger = true, enabled = false) {
            Text("Danger Disabled / Light Grey")
        }
        Spacer(modifier = Modifier.height(8.dp))
        BreakfastButtonRes(onClick = { }, iconRes = android.R.drawable.ic_menu_send) {
            Text("With Icon")
        }
    }
}
