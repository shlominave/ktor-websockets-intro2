package users
import users.UsersJsonFile.getAllUsersFromFile
import users.UsersJsonFile.getFile
import users.UsersJsonFile.updateFileContent

object UserValidator {
    private val usersFile = getFile()
    fun addUserToJsonIfValid(username: String, password: String): Boolean {
        val user = User(username, password)
        if (usersFile.exists()) {
            usersFile.createNewFile()
            updateFileContent(listOf(user))
            return true
        }
        if (usersFile.readText().isEmpty()) {
            updateFileContent(listOf(user))
            return true
        }
        var allUsers: List<User>? = getAllUsersFromFile()
        val isUsernameOK = !(isUsernameTaken(allUsers, user))
        if (isUsernameOK) {
            allUsers = allUsers?.plus(user)
            updateFileContent(allUsers)
        }
        return isUsernameOK
    }
    private fun isUsernameTaken(allUsers: List<User>?, user: User) =
        allUsers?.find { it.username == user.username} != null
}

