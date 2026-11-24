package com.vigil.app.domain

/**
 * Words for the counter. Variants are chosen by the day number and the date,
 * so the same milestone never shows the same line twice in a row.
 */
object StreakVoice {

    private val dayLines = listOf(
        "Day one. You chose today.",
        "Two days. A pattern is already forming.",
        "Three days. The first stretch is real now.",
        "Four days. Quietly done, quietly yours.",
        "Five days. Keep the bar low and the door closed.",
        "Six days. Tomorrow is right around the corner.",
        "A full week. That is a whole week you kept.",
        "Eight days. The habit is getting lighter.",
        "Nine days. Your future self is taking notes.",
        "Ten days. Double digits, earned one day at a time.",
        "Eleven days. You are not counting, you are living.",
        "Twelve days. Steady beats dramatic.",
        "Thirteen days. One more to a fortnight.",
        "Two weeks. Fourteen days of showing up.",
        "Fifteen days. Half a month, fully yours.",
        "Sixteen days. Boredom is not a setback.",
        "Seventeen days. The urge is weather, you are the sky.",
        "Eighteen days. Nothing to prove, just another day.",
        "Nineteen days. Almost three weeks clean.",
        "Twenty days. You have built real distance.",
        "Twenty-one days. A month is in sight now.",
        "Twenty-two days. Keep the evenings kind to you.",
        "Twenty-three days. Momentum is quiet work.",
        "Twenty-four days. You have done this before today.",
        "Twenty-five days. A quarter of a hundred.",
        "Twenty-six days. Almost four weeks.",
        "Twenty-seven days. The nights get easier.",
        "Twenty-eight days. Four whole weeks.",
        "Twenty-nine days. One day from a month.",
        "Thirty days. A full month you kept for yourself."
    )

    /** Several lines per day bucket so repeated streaks never feel identical. */
    private val dayVariants = listOf(
        listOf(
            "Day one. You chose today.",
            "The first clean day. Remember how this felt.",
            "This is where it began. One day, fully yours.",
            "One down. The rest are just repeats of this choice.",
            "A fresh run. Start gently and keep the line."
        ),
        listOf(
            "Two clean days. You are already building something.",
            "A pattern is forming. Let it.",
            "The second day is quieter than the first. That is good.",
            "Two days kept. Nothing flashy, just real.",
            "Keep the rhythm going."
        ),
        listOf(
            "Three days. The first stretch is real now.",
            "Three clean mornings in a row. Collect them.",
            "Day three. The habit is starting to hold.",
            "You are past the very beginning. Keep going.",
            "Three days of choosing yourself."
        ),
        listOf(
            "One week clean. That time is yours to keep.",
            "Seven days. A whole week you kept.",
            "A week of small no's adding up to something big.",
            "Seven days in. The nights should feel a little lighter.",
            "One full week. Let that be enough to keep going."
        ),
        listOf(
            "Two weeks clean. Fourteen quiet victories.",
            "A fortnight of showing up for yourself.",
            "Fourteen days. That is a pattern, not luck.",
            "Two weeks in. You are doing the work.",
            "Half a month kept. Keep the method."
        ),
        listOf(
            "Thirty days clean. A full month you kept.",
            "A month of decisions, stacked one on another.",
            "Thirty days. Look what quiet consistency built.",
            "One month clean. That is real evidence.",
            "The first month is yours. Keep going."
        )
    )

    private val later = listOf(
        "Every day after thirty is a day you chose.",
        "The number grows because you keep deciding.",
        "You are living this now, not performing it.",
        "Quiet consistency. That is the whole method.",
        "You do not need to be perfect today, just here.",
        "Long streaks are made of short decisions.",
        "You have done the hard part many times. Keep going.",
        "This is who you are now. Someone who keeps going.",
        "The streak is a record of your word kept.",
        "Keep the evenings boring and the days yours."
    )

    private val comebacks = listOf(
        "You came back. That is the whole skill.",
        "A new run, with everything you learned carried over.",
        "Returning is not starting over. It is starting wiser.",
        "Here again, and that matters more than a perfect record.",
        "Every return makes the next day easier.",
        "You came back sooner. That is progress.",
        "The comeback is where the growth happens.",
        "Start again from now, not from guilt.",
        "You have done this before. You can do it again.",
        "One more try is never wasted.",
        "The next run gets everything this one taught you.",
        "Return quietly. Stay consistently.",
        "The only wrong move is not returning.",
        "Each restart makes the next day easier.",
        "You are allowed to need more than one try.",
        "Begin again with less drama and more plan."
    )

    private val firstPass = listOf(
        "Your first clean day. Remember how this felt.",
        "This is where it began. One day, fully yours.",
        "The first one counts the most. You did it.",
        "One honest day is worth a hundred perfect intentions.",
        "This is the seed. Water it."
    )

    /** Line under the big number. Variety is keyed on day + date. */
    fun forDay(
        days: Long,
        best: Long,
        comebackCount: Int,
        firstEverPass: Boolean,
        justReturned: Boolean,
        dateSeed: Long = java.time.LocalDate.now().toEpochDay()
    ): String {
        if (firstEverPass) return firstPass[Math.floorMod(dateSeed, firstPass.size.toLong()).toInt()]
        if (justReturned) return comebacks[Math.floorMod(dateSeed + comebackCount, comebacks.size.toLong()).toInt()]

        val bucket = when (days) {
            1L -> 0
            2L -> 1
            3L -> 2
            in 4L..6L -> 1
            in 7L..13L -> 3
            in 14L..29L -> 4
            in 30L..59L -> 5
            else -> -1
        }
        return if (bucket >= 0) {
            val pool = dayVariants[bucket]
            pool[Math.floorMod(dateSeed + days, pool.size.toLong()).toInt()]
        } else {
            later[Math.floorMod(dateSeed + days, later.size.toLong()).toInt()]
        }
    }

    /** Setback: show what was built and give a concrete next target. */
    fun forReturn(previousDays: Long, best: Long, nextMarker: Long): String {
        val lines = listOf(
            "You reached $previousDays days. That time is real and it stays yours. Next marker: $nextMarker.",
            "Your best is $best days. That capacity is still yours. Next marker: $nextMarker.",
            "Come back sooner than last time. Next marker: $nextMarker.",
            "The days you kept were real. Begin again from now. Next marker: $nextMarker.",
            "A new run, with everything you learned carried over. Next marker: $nextMarker.",
            "Start again from now. Next marker: $nextMarker.",
            "Every comeback makes the next one easier. Next marker: $nextMarker."
        )
        val seed = java.time.LocalDate.now().toEpochDay()
        return lines[Math.floorMod(seed, lines.size.toLong()).toInt()]
    }

    /** A second line under the streak for extra motivation without clutter. */
    fun secondLine(days: Long, dateSeed: Long = java.time.LocalDate.now().toEpochDay()): String {
        val pool = listOf(
            "One day at a time.",
            "Keep the evenings boring and the mornings calm.",
            "The number is evidence, not your worth.",
            "Stay for the next hour, not forever.",
            "You have already done harder things.",
            "Quiet consistency beats intensity.",
            "No drama. Just keep the promise.",
            "Your future self is watching this choice.",
            "This is what showing up looks like.",
            "Keep going. That is the whole trick."
        )
        return pool[Math.floorMod(dateSeed + days, pool.size.toLong()).toInt()]
    }
}
