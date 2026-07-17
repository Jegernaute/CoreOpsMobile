package com.example.coreops.data.remote.models

import com.google.gson.annotations.SerializedName

data class ResourceDto(
    val id: Int,
    val name: String?,
    val url: String?,
    val file: String?,

    @SerializedName("resource_type")
    val resourceType: String?,

    @SerializedName("file_size")
    val fileSize: Long?,
    @SerializedName("file_extension")
    val fileExtension: String?
)