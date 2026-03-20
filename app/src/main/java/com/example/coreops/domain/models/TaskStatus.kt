package com.example.coreops.domain.model

/**
 * Перерахування всіх можливих статусів задачі згідно з бекендом.
 * @param apiValue - рядок який очікує і повертає Django бекенд.
 * @param displayName - гарна назва для відображення в UI.
 */
enum class TaskStatus(val apiValue: String, val displayName: String) {
    TODO("to_do", "To Do"),
    IN_PROGRESS("in_progress", "In Progress"),
    REVIEW("review", "Code Review"),
    DONE("done", "Done");

    companion object {
        /**
         * Допоміжна функція для безпечного перетворення рядка з бекенду в Enum.
         * Якщо прийде щось невідоме за замовчуванням поверне TODO.
         */
        fun fromApiValue(value: String): TaskStatus {
            return entries.find { it.apiValue == value } ?: TODO
        }
    }

    /**
     * Логіка переходу до наступного статусу (Workflow).
     * Допоможе перемикати статуси по кліку на UI.
     */
    fun next(): TaskStatus {
        return when (this) {
            TODO -> IN_PROGRESS
            IN_PROGRESS -> REVIEW
            REVIEW -> DONE
            DONE -> TODO // Робить циклічним щоб можна було повернути з Done назад в To Do якщо потрібно
        }
    }
}