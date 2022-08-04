package messages

import connections.WebSocketConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MessageFormatter{
    var message:String=""
    fun findTargetedUsernameFromMsg(beforeTargetedUsername: String, afterTargetedUsername: String) =
        message.getBetween("$beforeTargetedUsername:",afterTargetedUsername)
    private fun String.getBetween(before:String, after:String)=
              this.split(before,after).component2()
    fun setMessageContent(
        thisConnection: WebSocketConnection,
        receivedText: String
    ) = getMessageDateAndTime() + " Here's a message from " + thisConnection.username +":"+receivedText

    fun getMessageDateAndTime(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

    fun getPrivateMessageReceivingConnection(
        connections: MutableSet<WebSocketConnection>,
        targetedUsername: String
    ): WebSocketConnection? = connections.find { it.username == targetedUsername }
    //returns null if connection was not found
}
