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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.breakfast.R


@Composable
fun CollectorTableHeader(showAction: Boolean = false) {
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
            color = Color.White,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = stringResource(id = R.string.quantity),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.price),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.total_price),
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (showAction) {
            Text(
                text = stringResource(id = R.string.action),
                color = Color.White,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
