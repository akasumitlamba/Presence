package app.presence.desktop

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class AdaptiveLayoutTest {
    @Test fun tabletPortraitUsesRailWithoutForcingTwoPanes() {
        val tablet = UiLayout(700.dp, 1000.dp)
        assertTrue(tablet.rail)
        assertFalse(tablet.twoPanes)
    }
    @Test fun expandedWindowUsesTwoPanesButShortWindowsStayScrollable() {
        assertTrue(UiLayout(1000.dp, 700.dp).twoPanes)
        assertFalse(UiLayout(1000.dp, 400.dp).twoPanes)
    }
    @Test fun phonePortraitKeepsBottomNavigation() {
        assertFalse(UiLayout(360.dp, 800.dp).rail)
    }
}
