package com.breakfast.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.breakfast.R

interface CollectorOrderDisplayItem {
    val displayName: String?
    val displayQuantity: Int?
    val displayPrice: Double?
    val displayTotal: Double?
    val displayNote: String?
    val displayUsers: List<com.breakfast.models.Collector>?
}

@Composable
fun CollectorOrderItemCard(
    item: CollectorOrderDisplayItem,
    onShowUsers: ((List<com.breakfast.models.Collector>) -> Unit)? = null,
    onDeleteItem: ((CollectorOrderDisplayItem) -> Unit)? = null,
    showInfoAction: Boolean = true,
    showDeleteAction: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.card_bg), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.displayName ?: "--",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = (item.displayQuantity ?: 0).toString(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.displayPrice ?: 0.0} ${stringResource(com.breakfast.R.string.currency)}",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.displayTotal ?: 0.0} ${stringResource(com.breakfast.R.string.currency)}",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (showInfoAction && !item.displayUsers.isNullOrEmpty()) {
                IconButton(onClick = { onShowUsers?.invoke(item.displayUsers ?: emptyList()) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color(0xFF0D5BFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (showDeleteAction) {
                IconButton(onClick = { onDeleteItem?.invoke(item) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash_fill),
                        contentDescription = null,
                        tint = colorResource(id = R.color.punch),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (!item.displayNote.isNullOrEmpty()) {
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "${stringResource(com.breakfast.R.string.note)}: ${item.displayNote}",
                color = colorResource(com.breakfast.R.color.punch),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectorOrderItemCardPreview() {
    val sample = object : CollectorOrderDisplayItem {
        override val displayName: String? = "فول بلدي"
        override val displayQuantity: Int? = 2
        override val displayPrice: Double? = 15.0
        override val displayTotal: Double? = 30.0
        override val displayNote: String? = "بدون طحينة"
        override val displayUsers: List<com.breakfast.models.Collector>? = emptyList()
    }
    CollectorOrderItemCard(
        item = sample,
        onShowUsers = {},
        showInfoAction = false
    )
}


data class HistoryItemAdapter(private val src: com.breakfast.models.OrderHistoryItemModel) : CollectorOrderDisplayItem {
    override val displayName: String? get() = src.itemName
    override val displayQuantity: Int? get() = src.quantity
    override val displayPrice: Double? get() = src.price
    override val displayTotal: Double? get() = src.totalPrice
    override val displayNote: String? get() = src.note
    override val displayUsers: List<com.breakfast.models.Collector>? get() = src.users
}