package app.schedula.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)