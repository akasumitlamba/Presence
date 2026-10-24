package app.presence.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class StatisticsTest {
    @Test fun weekUsesMondayBoundariesAcrossYearEnd() {
        assertEquals(LocalDate.of(2025, 12, 29) to LocalDate.of(2026, 1, 4), periodBounds(1, LocalDate.of(2026, 1, 1)))
        assertEquals(LocalDate.of(2026, 10, 19) to LocalDate.of(2026, 10, 25), periodBounds(1, LocalDate.of(2026, 10, 25)))
    }
    @Test fun calendarMonthsAndYearsIncludeTheirActualLastDay() {
        assertEquals(LocalDate.of(2024, 2, 1) to LocalDate.of(2024, 2, 29), periodBounds(2, LocalDate.of(2024, 2, 20)))
        assertEquals(LocalDate.of(2026, 1, 1) to LocalDate.of(2026, 12, 31), periodBounds(3, LocalDate.of(2026, 10, 25)))
        assertEquals(LocalDate.of(2026, 2, 28), shiftPeriod(2, LocalDate.of(2026, 1, 31), 1))
    }
    @Test fun dayStartIsResolvedBeforeCalendarPeriodSelection() {
        val now = ZonedDateTime.of(2026, 3, 1, 1, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        val day = dayKey(now, ZoneId.of("UTC"), 2)
        assertEquals(LocalDate.of(2026, 2, 1) to LocalDate.of(2026, 2, 28), periodBounds(2, day))
        assertEquals(day to day, periodBounds(0, day))
    }
}
