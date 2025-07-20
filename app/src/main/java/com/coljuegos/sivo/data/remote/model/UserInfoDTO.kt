package com.coljuegos.sivo.data.remote.model

data class UserInfoDTO(

    @SerializedName("idUser")
    val idUser: String,

    @SerializedName("nameUser")
    val nameUser: String,

    @SerializedName("emailUser")
    val emailUser: String

)
