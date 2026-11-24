package com.vigil.app.domain

/**
 * Words for the counter. The number is the fact; these lines are only context.
 * No shaming, no scorekeeping of a person's worth.
 */
object StreakVoice {

    /** One line for each of the first 30 clean days. */
    private val days = listOf(
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

    private val firstPass = listOf(
        "Your first clean day. Remember how this felt.",
        "This is where it began. One day, fully yours.",
        "The first one counts the most. You did it."
    )

    private val comebacks = listOf(
        "You came back. That is the whole skill.",
        "A new run, with everything you learned carried over.",
        "Returning is not starting over. It is starting wiser.",
        "Here again, and that matters more than a perfect record.",
        "Every return makes the next day easier."
    )

    private val later = listOf(
        "Every day after thirty is a day you chose.",
        "The number grows because you keep deciding.",
        "You are living this now, not performing it.",
        "Quiet consistency. That is the whole method.",
        "You do not need to be perfect today, just here."
    )

    /** Line shown under the big clean-days number. */
    fun forDay(
        days: Long,
        best: Long,
        comebackCount: Int,
        firstEverPass: Boolean,
        justReturned: Boolean
    ): String = when {
        firstEverPass -> firstPass[comebackCount.coerceAtLeast(0) % firstPass.size]
        justReturned -> comebacks[comebackCount.coerceAtLeast(0) % comebacks.size]
        days in 1L..30L -> days.get((days - 1).toInt())
        days > 30L -> later.get((days % later.size).toInt())
        else -> "Today is day one. It counts."
    }

    /** Shown when someone records a setback, so the next run has a target. */
    fun forReturn(previousDays: Long, best: Long, nextMarker: Long): String = when {
        previousDays >= best && previousDays > 0 ->
            "You reached $previousDays days — your best yet. That capacity is still yours. Next marker: $nextMarker."
        best > 0 ->
            "You reached $previousDays days. Your best is $best — that time is real and it stays yours. Next marker: $nextMarker."
        else ->
            "Every run teaches you something. Next marker: $nextMarker. Begin again from now."
    }
}
