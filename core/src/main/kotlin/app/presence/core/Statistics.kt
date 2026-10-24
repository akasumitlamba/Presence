package app.presence.core

import java.time.LocalDate
import java.time.YearMonth

/** Calendar boundaries use the already resolved logical day, including the user's day-start hour. */
fun periodBounds(range: Int, anchor: LocalDate): Pair<LocalDate, LocalDate> = when (range) {
    1 -> anchor.minusDays(anchor.dayOfWeek.value - 1L).let { it to it.plusDays(6) }
    2 -> YearMonth.from(anchor).let { it.atDay(1) to it.atEndOfMonth() }
    3 -> LocalDate.of(anchor.year, 1, 1) to LocalDate.of(anchor.year, 12, 31)
    else -> anchor to anchor
}

fun shiftPeriod(range: Int, anchor: LocalDate, amount: Long): LocalDate = when (range) {
    1 -> anchor.plusWeeks(amount)
    2 -> anchor.plusMonths(amount)
    3 -> anchor.plusYears(amount)
    else -> anchor.plusDays(amount)
}
