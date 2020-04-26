package zeromq.core

import org.{zeromq => jeromq}
import zio._

object Socket {

  trait Service {
    def bind(address: String): Task[Boolean]
    def receive(flags: Int): Task[Option[Bytes]]
    def send(bytes: Bytes, flags: Int): Task[Boolean]
    def close: Task[Unit]
  }

  final class Live[T] private (jSocket: jeromq.ZMQ.Socket) extends Service {
    override def bind(address: String): Task[Boolean] = Task {
      jSocket.bind(address)
    }

    override def receive(flags: Int): Task[Option[Bytes]] = Task {
      Option(jSocket.recv(flags))
    }

    override def send(bytes: Bytes, flags: Int): Task[Boolean] = Task {
      jSocket.send(bytes, flags)
    }

    override def close: Task[Unit] = Task {
      jSocket.close()
    }
  }

  object Live {
    def open(jContext: jeromq.ZContext, socketType: jeromq.SocketType): ZManaged[Any, Throwable, Socket.Service] =
      UIO {
        new Live(jContext.createSocket(socketType))
      }.toManaged(_.close.orDie)
  }

  def live(jContext: jeromq.ZContext, socketType: jeromq.SocketType): ZLayer[Any, Throwable, Socket] =
    Live.open(jContext, socketType).toLayer
}
