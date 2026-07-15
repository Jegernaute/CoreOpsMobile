package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    val avatar: String?,

    @SerializedName("job_title")
    val jobTitle: String?,

    val phone: String?,
    val telegram: String?,

    @SerializedName("global_role")
    val globalRole: String
) {
    // Зручна властивість для відображення в UI
    val fullName: String
        get() = "$firstName $lastName".trim()

    // Безпечний мапінг URL аватара для Coil
    val safeAvatarUrl: String?
        get() {
            if (avatar.isNullOrBlank()) return null
            if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar

            // Базовий URL для емулятора (змінити на реальний IP мережі 192.168.X.X, якщо це фізичний пристрій)
            val fallbackBaseUrl = "http://10.0.2.2:8000"

            return if (avatar.startsWith("/")) {
                "$fallbackBaseUrl$avatar"
            } else {
                "$fallbackBaseUrl/$avatar"
            }
        }
}
