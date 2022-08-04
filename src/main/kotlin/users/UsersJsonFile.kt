package users

import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

object UsersJsonFile {
    private val usersFile = File("src/main/resources/allUsers.json")
    private val gson = Gson()
    fun getFile()=usersFile
    fun updateFileContent(userList: List<User>?) {
        val usersWriter = FileWriter(usersFile)
        gson.toJson(userList, usersWriter)
        usersWriter.close()
    }
    fun getAllUsersFromFile(): List<User>? {
        if (usersFile.exists()) {
            if (usersFile.readText().isNotEmpty())
                return gson.fromJson(usersFile.readText(), Array<User>::class.java).toList()
        }
        return null
    }
}