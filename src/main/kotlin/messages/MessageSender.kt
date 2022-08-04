package messages

import connections.WebSocketConnection
import io.ktor.server.websocket.*
import io.ktor.websocket.*

object MessageSender {
    suspend fun DefaultWebSocketServerSession.sendNonPrivateMessage(
        connections: MutableSet<WebSocketConnection>,
        thisConnection: WebSocketConnection,
        receivedText: String,
        messageFormatter: MessageFormatter
    ) {
        connections.forEach {
            val message = messageFormatter.setMessageContent(thisConnection, receivedText)
            if (it.username == thisConnection.username) {
                send(message.replace("from $thisConnection", "you've just sent"))
            } else
                it.session.send(message)
        }
    }

    suspend fun sendNewUserConnectedMsg(connections: MutableSet<WebSocketConnection>) {
        connections.forEach {
            if (it != connections.last())
                it.session.send(
                    "a new user named ${connections.last().username} has just logged in. ${System.lineSeparator()}" +
                            "Now There are ${connections.count()} users here"
                )
        }
    }

    suspend fun DefaultWebSocketServerSession.sendPM(
        targetedConnection: WebSocketConnection,
        thisConnection: WebSocketConnection,
        receivedText: String,
        msg: MessageFormatter
    ) {
        val privateMessageCommand = targetedConnection.username + "/msg"
        if (targetedConnection.username == thisConnection.username)
            send(
                msg.getMessageDateAndTime() + " Here's a private message to myself:${
                    receivedText.removePrefix(
                        privateMessageCommand
                    )
                }"
            )
        else {
            val message = msg.setMessageContent(thisConnection, receivedText.removePrefix(privateMessageCommand))
            targetedConnection.session.send("$message(private message)")
            send(
                "${msg.getMessageDateAndTime()} I've sent a private message:${
                    receivedText.removePrefix(
                        privateMessageCommand
                    )
                }"
            )
        }
    }

    suspend fun sendUserDisconnectedMessage(
        connections: MutableSet<WebSocketConnection>,
        thisConnection: WebSocketConnection
    ) {
        connections.forEach {
            it.session.send(
                " $thisConnection! is longer connected ${System.lineSeparator()}" +
                        "number of users still connected:${connections.count()} "
            )
        }
    }
}