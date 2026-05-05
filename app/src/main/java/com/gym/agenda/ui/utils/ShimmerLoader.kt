package com.gym.agenda.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gym.agenda.ui.theme.GymAgendaTheme
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerLoader(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    if (!isLoading) return

    GymAgendaTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header shimmer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shimmer(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors()
            ) {
                // Empty content for shimmer effect
            }

            // List items shimmer
            repeat(5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar placeholder
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .shimmer(),
                        shape = RoundedCornerShape(25.dp),
                        colors = CardDefaults.cardColors()
                    ) {}

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .shimmer(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors()
                        ) {}

                        // Subtitle
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(14.dp)
                                .shimmer(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors()
                        ) {}
                    }
                }
            }
        }
    }
}
