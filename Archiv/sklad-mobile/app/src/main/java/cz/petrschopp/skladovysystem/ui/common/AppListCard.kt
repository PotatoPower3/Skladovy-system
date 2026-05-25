package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> AppListCard(
    items: List<T>,
    emptyText: String,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int? = null,
    maxHeightDp: Int? = null,
    rowContent: @Composable (T) -> Unit
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (maxHeightDp != null) {
                    Modifier.heightIn(min = 120.dp, max = maxHeightDp.dp)
                } else {
                    Modifier
                }
            )
    ) {
        if (items.isEmpty()) {
            Text(
                text = emptyText,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            val visibleItems = maxVisibleItems?.let { items.take(it) } ?: items

            LazyColumn(
                modifier = Modifier.padding(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(visibleItems) { index, item ->
                    rowContent(item)

                    if (index < visibleItems.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }
    }
}