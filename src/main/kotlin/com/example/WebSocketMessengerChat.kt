package com.example

import connections.WebSocketConnection
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import messages.MessageSender
import messages.MessageFormatter
import users.*
import java.io.File
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

class WebSocketMessengerChat(private val application: Application) {
    fun configureSockets() {
        application.install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        application.routing {
            val connections = Collections.synchronizedSet<WebSocketConnection?>(LinkedHashSet())
            webSocket("/chat") {
                var user: User? = User("", "")
                try {
                    while (true) {
                        send(
                            "Hello and welcome to my chat project!" + System.lineSeparator() +
                                    "write R/r to register or L/l to login as an existing user"
                        )
                        user = user?.connectingUser(this, connections)
                        if (user != null)
                            break
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                }
                val thisConnection = WebSocketConnection(this, user?.username ?: "")
                connections += thisConnection
                val msgSender = MessageSender
                try {
                    send(
                        "You are connected! There are ${connections.count()} users here.${System.lineSeparator()} " +
                                "to send a PM use 'targeted username/msg' command "
                    )

                    msgSender.sendNewUserConnectedMsg(connections)
                    val privateMessageKey = "/msg"
                    for (frame in incoming) {
                        if (!File("src/main/resources/allUsers.json").exists())
                            break
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        if (receivedText == "quit") {
                            send("quiting..")
                            break
                        }
                        val msgFormatter = MessageFormatter
                        val isPM = receivedText.contains(privateMessageKey)
                        if (isPM) {
                            msgFormatter.message = msgFormatter.setMessageContent(thisConnection, receivedText)
                            val targetedConnection: WebSocketConnection? =
                                thisConnection.findTargetedConnection(msgFormatter, connections)
                            if (targetedConnection != null) {
                                with(msgSender){
                               sendPM(targetedConnection, thisConnection, receivedText, msgFormatter)
                                }
                            } else {
                                thisConnection.session.send("failed to send $receivedText because targeted username was not found")
                            }
                        } else {
                            with(msgSender){
                            sendNonPrivateMessage(connections, thisConnection, receivedText, msgFormatter)
                            }
                        }
                    }
                } catch (e: Exception) {
                    send("there is an error")
                    println(e.localizedMessage)
                } finally {
                    send("disconnecting $thisConnection!")
                    connections -= thisConnection
                    msgSender.sendUserDisconnectedMessage(connections, thisConnection)
                }
            }
        }
    }
}