package app.schedula.data.model

data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val rating: Double = 0.0,
    val experience: Int = 0,
    val gender: String = "",
    val fee: Int = 0
)
