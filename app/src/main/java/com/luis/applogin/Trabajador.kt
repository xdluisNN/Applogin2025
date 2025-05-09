package com.luis.applogin

data class Trabajador(
    val direccion: String = "",
    val email: String = "",
    val empresaId: String? = null,
    val nombre: String = "",
    val rol: String = "",
    val telefono: String = ""
)
