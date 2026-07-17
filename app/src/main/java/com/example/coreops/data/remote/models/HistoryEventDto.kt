package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class HistoryEventDto(
    val id: Int,
    val actor: UserDetailsDto,
    val timestamp: String,
    @SerializedName("action_type")
    val actionType: String,
    val changes: Map<String, ChangeDetail>
)

data class ChangeDetail(
    @SerializedName("old_value")
    val oldValue: String?,
    @SerializedName("new_value")
    val newValue: String?
)