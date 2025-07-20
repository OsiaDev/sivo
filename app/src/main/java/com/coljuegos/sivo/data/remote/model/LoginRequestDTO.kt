package com.coljuegos.sivo.data.remote.model

data class LoginRequestDTO(

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String

)
