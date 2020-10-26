package com.zhangmin.meetup.utils

import io.circe.{Decoder, parser}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import scalawebsocket.WebSocket

class WebSocketReceiver[T: Decoder](websocketUrl: String) extends Receiver[T](StorageLevel.MEMORY_ONLY) {

  @volatile private var webSocket: WebSocket = _

  override def onStart(): Unit =
    try {
      val newWebSocket = WebSocket()
        .open(websocketUrl)
        .onTextMessage({ msg: String =>
          parser.decode[T](msg) match {
            case Left(error) =>
              // TODO increment parsing failure metric and add alerts on said metric
              println("Could not decode text stream from websocket", error)
            case Right(rsvp) => store(rsvp)
          }
        })
      setWebSocket(newWebSocket)
    } catch {
      case e: Exception => restart("Error starting WebSocket stream", e)
    }

  override def onStop(): Unit = setWebSocket(null)

  private def setWebSocket(newWebSocket: WebSocket): Unit = synchronized {
    if (webSocket != null) {
      webSocket.shutdown()
    }
    webSocket = newWebSocket
  }

}
