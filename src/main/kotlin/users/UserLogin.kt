package users

import connections.WebSocketConnection
import io.ktor.websocket.*
import modules.LoginState

object UserLogin {
    private var username = ""
    private var password = ""
    private val usersJson=UsersJsonFile
    suspend fun login(session: DefaultWebSocketSession, connections: MutableList<WebSocketConnection>): User? {
        session.send("if you wish to return to the menu write (my menu) in one of the fields")
        session.send("Welcome to login ${System.lineSeparator()} Enter username")
        username = (session.incoming.receive() as Frame.Text).readText()
        val toMenuCommand = "my menu"
        if (username.contains(toMenuCommand)) {
            session.send("back to the menu!")
            return null
        }
        session.send("Enter Password")
        password = (session.incoming.receive() as Frame.Text).readText()
        if (password.contains(toMenuCommand)) {
            session.send("back to the menu!")
            return null
        }
        try {
            if (username.isNotEmpty() && password.isNotEmpty()) {
                val loginUser = User(username, password)
                val loginState = validateLoginAndRespond(loginUser, connections)
                session.send(loginState.getResponse())
                if (loginState == LoginState.LOGINSUCCEEDED) {
                    return loginUser
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
        return null
    }

    private fun validateLoginAndRespond(
        user: User,
        connections: MutableList<WebSocketConnection>
    ): LoginState {
        if (!usersJson.getFile().exists())
            usersJson.getFile().createNewFile()
        val users: List<User>? = usersJson.getAllUsersFromFile()
        if (!users.isNullOrEmpty()) {
            if (users.find {
                    it.username == user.username && it.password == user.password
                } == null)
                return LoginState.TRYAGAIN
            if (connections.find { it.username == user.username } != null)
                return LoginState.ALREADYLOGGEDIN
            return LoginState.LOGINSUCCEEDED
        }
        return LoginState.NOUSERSFOUND
    }
}