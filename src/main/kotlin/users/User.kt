package users

import connections.WebSocketConnection
import io.ktor.websocket.*
import modules.UserOption

data class User(val username: String, val password: String) {
    private fun hasEmptyField(): Boolean = this.username.isEmpty() || this.password.isEmpty()
    override fun toString(): String {
        return "[username:$username password:$password]"
    }
    suspend fun connectingUser (defaultWebSocketSession: DefaultWebSocketSession,
        connections: MutableSet<WebSocketConnection>
    ): User? {
        val userRegister = UserRegister
        val userLogin = UserLogin
        val user: User?
        with(defaultWebSocketSession)
        {
            val choice = (incoming.receive() as Frame.Text).readText()
            when (choice.lowercase()) {
                UserOption.REGISTEROPTION.userChoice() -> {
                    send("going register")
                    if (userRegister.registerSucceeded(this)) {
                        user = User(userRegister.getUsername(), userRegister.getPassword())
                        if (!user.hasEmptyField()) {
                            send("You are logged in!")
                            return user
                        } else {
                            send("auto login failed,try again later")
                        }
                    }
                }
                UserOption.LOGINOPTION.userChoice() -> {
                    send("going login")
                    user = userLogin.login(this, connections.toMutableList())
                    if (user != null && !user.hasEmptyField())
                        return user
                }
                else -> {
                    send("invalid input, try again")
                }
        }
        }
        return null
    }
}