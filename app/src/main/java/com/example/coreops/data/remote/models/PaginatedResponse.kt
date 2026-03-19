package com.example.coreops.data.remote.models

/**
 * Універсальна обгортка для пагінації від Django REST Framework.
 * Використовує дженерик <T>, щоб потім можна було повторно використати
 * цей клас для задач (TaskDto), користувачів (UserDto) тощо.
 */
data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)