package app.presence.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Tall tablet windows prioritize a readable dial above the controls. */
internal fun timerUsesColumns(width: Dp, height: Dp): Boolean = width >= 620.dp && width >= height
