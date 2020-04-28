package zeromq

import org.{zeromq => jeromq}
import zio._

import scala.language.experimental.macros

object Socket {

  abstract sealed class SocketType(val jSocketType: jeromq.SocketType)
  object Pair   extends SocketType(jeromq.SocketType.PAIR)
  object Pub    extends SocketType(jeromq.SocketType.PUB)
  object Sub    extends SocketType(jeromq.SocketType.SUB)
  object Req    extends SocketType(jeromq.SocketType.REQ)
  object Rep    extends SocketType(jeromq.SocketType.REP)
  object Dealer extends SocketType(jeromq.SocketType.DEALER)
  object Router extends SocketType(jeromq.SocketType.ROUTER)
  object Pull   extends SocketType(jeromq.SocketType.PULL)
  object Push   extends SocketType(jeromq.SocketType.PUSH)
  object XPub   extends SocketType(jeromq.SocketType.XPUB)
  object XSub   extends SocketType(jeromq.SocketType.XSUB)
  object Stream extends SocketType(jeromq.SocketType.STREAM)

  trait Service[A <: SocketType] {
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

  final class Live[A <: SocketType] private (jSocket: jeromq.ZMQ.Socket) extends Service[A] {
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
    def open[A <: SocketType](socketType: A, context: Context.Service): ZManaged[Any, Nothing, Service[A]] =
      context.createSocket(socketType.jSocketType).map { new Live[A](_) }.toManaged(_.close.orDie).orDie
  }

  final class Type[A <: SocketType](socketType: A)(implicit t1: Tagged[Service[A]]) {

    def bind(address: String): RIO[Socket[A], Boolean]            = RIO.accessM(_.get.bind(address))
    def close: RIO[Socket[A], Unit]                               = RIO.accessM(_.get.close)
    def connect(address: String): RIO[Socket[A], Boolean]         = RIO.accessM(_.get.connect(address))
    def receive: RIO[Socket[A], Option[Bytes]]                    = RIO.accessM(_.get.receive)
    def receive(flags: Int): RIO[Socket[A], Option[Bytes]]        = RIO.accessM(_.get.receive(flags))
    def receiveString: RIO[Socket[A], Option[String]]             = RIO.accessM(_.get.receiveString)
    def receiveString(flags: Int): RIO[Socket[A], Option[String]] = RIO.accessM(_.get.receiveString(flags))
    def send(bytes: Bytes, flags: Int): RIO[Socket[A], Boolean]   = RIO.accessM(_.get.send(bytes, flags))
    def send(bytes: Bytes): RIO[Socket[A], Boolean]               = RIO.accessM(_.get.send(bytes))
    def send(string: String, flags: Int): RIO[Socket[A], Boolean] = RIO.accessM(_.get.send(string, flags))
    def send(string: String): RIO[Socket[A], Boolean]             = RIO.accessM(_.get.send(string))

    def live: ZLayer[Context, Nothing, Socket[A]] = ZLayer.fromServiceManaged(Live.open[A](socketType, _))
  }
}
