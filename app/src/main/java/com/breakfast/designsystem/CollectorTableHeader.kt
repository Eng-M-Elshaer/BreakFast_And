package com.breakfast.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breakfast.R


@Composable
fun CollectorTableHeader(showAction: Boolean = false) {
    val fontColor = colorResource(id = R.color.card_bg)
    val backgroundColor = colorResource(id = com.breakfast.R.color.blue_ribbon)
    val fontSize = 12.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorResource(id = com.breakfast.R.color.blue_ribbon),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.name),
            fontSize = fontSize,
            color = fontColor,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = stringResource(id = R.string.quantity),
            fontSize = fontSize,
            color = fontColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.price),
            fontSize = fontSize,
            color = fontColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.total),
            fontSize = fontSize,
            color = fontColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAction) {
            Text(
                text = stringResource(id = R.string.action),
                fontSize = fontSize,
                color = fontColor,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
