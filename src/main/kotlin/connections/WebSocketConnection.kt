package connections
import io.ktor.websocket.*
import messages.MessageFormatter

data class WebSocketConnection(val session: DefaultWebSocketSession, val username: String) {
  override fun toString():String{
        return this.username
    }
    fun findTargetedConnection(
        messageFormatter: MessageFormatter, connections: MutableSet<WebSocketConnection>,
    ): WebSocketConnection? {
        val privateMessageKey = "/msg"
        val targetedUsername = messageFormatter.findTargetedUsernameFromMsg(username, privateMessageKey)
        return messageFormatter.getPrivateMessageReceivingConnection(connections, targetedUsername)
    }
}