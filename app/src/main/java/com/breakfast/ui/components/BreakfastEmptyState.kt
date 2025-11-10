package com.breakfast.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.breakfast.designsystem.BreakfastButtonRes

@Composable
fun BreakfastEmptyState(
    iconRes: Int,
    title: String,
    showButton: Boolean = false,
    buttonText: String = "",
    onButtonClick: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(220.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (showButton) {
                Spacer(Modifier.height(16.dp))
                BreakfastButtonRes(
                    onClick = onButtonClick,
                    modifier = Modifier.height(52.dp)
                ) {
                    androidx.compose.material3.Text(text = buttonText)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun BreakfastEmptyStatePreview() {
    BreakfastEmptyState(
        iconRes = com.breakfast.R.drawable.no_history,
        title = "No history yet",
        showButton = true,
        buttonText = "Reload",
        onButtonClick = {}
    )
}