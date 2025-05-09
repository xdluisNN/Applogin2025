package com.luis.applogin

data class Trabajador(
    val uid: String? = null,
    val nombre: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val rol: String? = null,
    var empresaId: String? = null,
    var estado: String? = null
)

