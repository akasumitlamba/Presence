package app.presence.core

import java.util.Locale

const val KIND_GROUP = "group"
const val KIND_TAG = "tag"
const val KIND_PROJECT = "project"
const val KIND_TASK = "task"
const val KIND_SESSION = "session"
const val KIND_PREF = "pref"
const val KIND_LOCAL = "local"

val SYNC_KINDS = setOf(KIND_GROUP, KIND_TAG, KIND_PROJECT, KIND_TASK, KIND_SESSION, KIND_PREF)

val TIMER_PREF_IDS = setOf(
    "focus_min", "short_min", "long_min", "cycles", "count_up", "auto_start",
    "work_sec", "short_sec", "long_sec", "short_count", "long_count",
)

const val DEFAULT_WORK_MS = 3_600_000L
const val DEFAULT_SHORT_BREAKS = 2
const val DEFAULT_LONG_BREAKS = 0
const val DEFAULT_SHORT_MS = 300_000L
const val DEFAULT_LONG_MS = 900_000L

const val GROUP_WORK = "g-work"
const val GROUP_STUDY = "g-study"
const val GROUP_PERSONAL = "g-personal"

const val FREE_PROJECTS = 3
const val FREE_TASKS = 8
const val MIN_KEEP_MS = 5_000L
const val CODE_TTL_MS = 10 * 60 * 1000L

data class Rec(
    val kind: String,
    val id: String,
    val updatedAt: Long,
    val writerId: String,
    val deleted: Boolean,
    val body: Map<String, String>,
)

fun Rec.f(key: String): String = body[key].orEmpty()

fun List<Rec>.alive(kind: String): List<Rec> = filter { it.kind == kind && !it.deleted }

fun beats(incoming: Rec, current: Rec): Boolean {
    if (incoming.updatedAt != current.updatedAt) return incoming.updatedAt > current.updatedAt
    return incoming.writerId > current.writerId
}

fun mergeRecords(
    local: List<Rec>,
    incoming: List<Rec>,
    protectedSessionIds: Set<String>,
    deferredPrefIds: Set<String>,
): List<Rec> {
    val by = local.associateBy { it.kind to it.id }.toMutableMap()
    for (inc in incoming) {
        if (inc.kind !in SYNC_KINDS) continue
        if (inc.kind == KIND_SESSION && inc.id in protectedSessionIds) continue
        if (inc.kind == KIND_PREF && inc.id in deferredPrefIds) continue
        val key = inc.kind to inc.id
        val cur = by[key]
        if (cur == null || beats(inc, cur)) by[key] = inc
    }
    return by.values.toList()
}

enum class CodeStatus { Match, Wrong, Expired, NotOffering }

fun codeStatus(typed: String, offered: String?, offeredUntil: Long, now: Long): CodeStatus {
    if (offered.isNullOrEmpty()) return CodeStatus.NotOffering
    val digits = typed.trim()
    if (digits == offered && now >= offeredUntil) return CodeStatus.Expired
    if (digits != offered) return CodeStatus.Wrong
    return CodeStatus.Match
}

fun chooseCode(taken: Set<String>, draw: (Int) -> Int): String? {
    if (taken.size >= 10_000) return null
    repeat(64) {
        val code = fourDigits(draw(10_000))
        if (code !in taken) return code
    }
    for (i in 0..9999) {
        val code = fourDigits(i)
        if (code !in taken) return code
    }
    return null
}

private fun fourDigits(value: Int): String = String.format(Locale.ROOT, "%04d", value)

fun rotateCode(current: String?, others: Set<String>, draw: (Int) -> Int): String? {
    if (current != null && current !in others) return current
    return chooseCode(others, draw)
}
