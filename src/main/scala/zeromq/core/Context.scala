package zeromq.core

import zio.{Task, UIO}
import org.{zeromq => jeromq}

object Context {

  trait Service {
    def createSocket(socketType: jeromq.SocketType): UIO[Socket.Service]

    def close: Task[Unit]

  }
}
