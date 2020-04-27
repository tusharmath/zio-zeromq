package zeromq

import org.{zeromq => jeromq}
import zio.{Task, UIO, ZManaged}

import scala.language.experimental.macros
import zio.Has
import zio.RIO
import zio.ZLayer
import zio.ZIO

object Socket {
  final class Type(socketType: jeromq.SocketType) {
    private type HasService = Has[Service]
    trait Service {
      def bind(address: String): Task[Boolean]
      def close: Task[Unit]
      def receive: Task[Option[Bytes]]
      def receive(flags: Int): Task[Option[Bytes]]
      def receiveString: Task[Option[String]]
      def receiveString(flags: Int): Task[Option[String]]
      def send(bytes: Bytes, flags: Int): Task[Boolean]
      def send(bytes: Bytes): Task[Boolean]
      def send(data: String, flags: Int): Task[Boolean]
      def send(data: String): Task[Boolean]
    }

    class Live private (jSocket: jeromq.ZMQ.Socket) extends Service {
      override def bind(address: String): Task[Boolean]            = Task { jSocket.bind(address) }
      override def close: Task[Unit]                               = Task { jSocket.close() }
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
      def open(context: Context.Service): ZManaged[Any, Throwable, Service] =
        context.createSocket(socketType).map { new Live(_) }.toManaged(_.close.orDie)
    }

    def bind(address: String): RIO[HasService, Boolean]            = RIO.accessM(_.get.bind(address))
    def close: RIO[HasService, Unit]                               = RIO.accessM(_.get.close)
    def receive: RIO[HasService, Option[Bytes]]                    = RIO.accessM(_.get.receive)
    def receive(flags: Int): RIO[HasService, Option[Bytes]]        = RIO.accessM(_.get.receive(flags))
    def receiveString: RIO[HasService, Option[String]]             = RIO.accessM(_.get.receiveString)
    def receiveString(flags: Int): RIO[HasService, Option[String]] = RIO.accessM(_.get.receiveString(flags))
    def send(bytes: Bytes, flags: Int): RIO[HasService, Boolean]   = RIO.accessM(_.get.send(bytes, flags))
    def send(bytes: Bytes): RIO[HasService, Boolean]               = RIO.accessM(_.get.send(bytes))
    def send(string: String, flags: Int): RIO[HasService, Boolean] = RIO.accessM(_.get.send(string, flags))
    def send(string: String): RIO[HasService, Boolean]             = RIO.accessM(_.get.send(string))

    def live: ZLayer[Context, Throwable, HasService]                  = ZLayer.fromServiceManaged(Live.open(_))
    def live(address: String): ZLayer[Context, Throwable, HasService] = ZLayer.fromServiceManaged(Live.open(_, address))
    def use[R <: Has[_], E >: Throwable, A](zio: ZIO[HasService with R, E, A]): ZIO[Context with R, E, A] =
      zio.provideSomeLayer[Context with R](live)
  }
}
