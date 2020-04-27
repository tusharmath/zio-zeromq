package zeromq

import org.{zeromq => jeromq}
import zio.{Task, UIO, ZManaged}

import scala.language.experimental.macros
import zio.Has
import zio.RIO
import zio.ZLayer
import zio.ZIO
import zio.Tagged

object Socket {
  private type HasService[A] = Has[Service[A]]
  trait Service[A] {
    def bind(address: String): Task[Boolean]
    def close: Task[Unit]
    def connect(address: String): Task[Boolean]
    def receive: Task[Option[Bytes]]
    def receive(flags: Int): Task[Option[Bytes]]
    def receiveString: Task[Option[String]]
    def receiveString(flags: Int): Task[Option[String]]
    def send(bytes: Bytes, flags: Int): Task[Boolean]
    def send(bytes: Bytes): Task[Boolean]
    def send(data: String, flags: Int): Task[Boolean]
    def send(data: String): Task[Boolean]
  }

  final class Type[A <: jeromq.SocketType](socketType: A)(implicit t1: Tagged[Service[A]]) {
    class Live private (jSocket: jeromq.ZMQ.Socket) extends Service[A] {
      override def bind(address: String): Task[Boolean]            = Task { jSocket.bind(address) }
      override def close: Task[Unit]                               = Task { jSocket.close() }
      override def connect(address: String): Task[Boolean]         = Task { jSocket.connect(address) }
      override def receive: Task[Option[Bytes]]                    = Task { Option(jSocket.recv()) }
      override def receive(flags: Int): Task[Option[Bytes]]        = Task { Option(jSocket.recv(flags)) }
      override def receiveString: Task[Option[String]]             = Task { Option(jSocket.recvStr()) }
      override def receiveString(flags: Int): Task[Option[String]] = Task { Option(jSocket.recvStr(flags)) }
      override def send(bytes: Bytes, flags: Int): Task[Boolean]   = Task { jSocket.send(bytes, flags) }
      override def send(bytes: Bytes): Task[Boolean]               = Task { jSocket.send(bytes) }
      override def send(data: String, flags: Int): Task[Boolean]   = Task { jSocket.send(data, flags) }
      override def send(data: String): Task[Boolean]               = Task { jSocket.send(data) }
    }

    object Live {
      def open(context: Context.Service): ZManaged[Any, Throwable, Service[A]] =
        context.createSocket(socketType).map { new Live(_) }.toManaged(_.close.orDie)
    }

    def bind(address: String): RIO[HasService[A], Boolean]            = RIO.accessM(_.get.bind(address))
    def close: RIO[HasService[A], Unit]                               = RIO.accessM(_.get.close)
    def connect(address: String): RIO[HasService[A], Boolean]         = RIO.accessM(_.get.connect(address))
    def receive: RIO[HasService[A], Option[Bytes]]                    = RIO.accessM(_.get.receive)
    def receive(flags: Int): RIO[HasService[A], Option[Bytes]]        = RIO.accessM(_.get.receive(flags))
    def receiveString: RIO[HasService[A], Option[String]]             = RIO.accessM(_.get.receiveString)
    def receiveString(flags: Int): RIO[HasService[A], Option[String]] = RIO.accessM(_.get.receiveString(flags))
    def send(bytes: Bytes, flags: Int): RIO[HasService[A], Boolean]   = RIO.accessM(_.get.send(bytes, flags))
    def send(bytes: Bytes): RIO[HasService[A], Boolean]               = RIO.accessM(_.get.send(bytes))
    def send(string: String, flags: Int): RIO[HasService[A], Boolean] = RIO.accessM(_.get.send(string, flags))
    def send(string: String): RIO[HasService[A], Boolean]             = RIO.accessM(_.get.send(string))

    def live: ZLayer[Context, Throwable, HasService[A]] = ZLayer.fromServiceManaged(Live.open(_))
  }
}
