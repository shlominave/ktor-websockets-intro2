package users

import io.ktor.websocket.*

object UserRegister {
    private var username = ""
    private var password = ""
    fun getUsername()=username
    fun getPassword()=password
    suspend fun registerSucceeded(session: DefaultWebSocketSession): Boolean {
        session.send("if you wish to return to the menu write (my menu) in one of the fields")
        session.send("Welcome to register ${System.lineSeparator()} Enter Username")
        username = (session.incoming.receive() as Frame.Text).readText()
        if (username.contains("my menu")) {
            session.send("back to the menu!")
            return false
        }
        session.send("Enter Password")
        password = (session.incoming.receive() as Frame.Text).readText()
        if (password.contains("my menu")) {
            session.send("back to the menu!")
            return false
        }
        if (username.length >= 3 && password.length >= 3) {
            try {
                val usersJson = UserValidator
                if (usersJson.addUserToJsonIfValid(username, password)) {
                    session.send("Registration completed successfully")
                    return true
                } else
                    session.send("username is taken, try again")
            } catch (e: Exception) {
                println("HA NOPE")
                println(e.localizedMessage)
            }
        } else session.send("username and password both need to be at least 3 chars long")

        return false
    }
}
