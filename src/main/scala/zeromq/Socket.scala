package zeromq

import org.{zeromq => jeromq}
import zio._

import scala.language.experimental.macros

object Socket {

  abstract sealed class SocketType(val jSocketType: jeromq.SocketType)
  object PAIR   extends SocketType(jeromq.SocketType.PAIR)
  object PUB    extends SocketType(jeromq.SocketType.PUB)
  object SUB    extends SocketType(jeromq.SocketType.SUB)
  object REQ    extends SocketType(jeromq.SocketType.REQ)
  object REP    extends SocketType(jeromq.SocketType.REP)
  object DEALER extends SocketType(jeromq.SocketType.DEALER)
  object ROUTER extends SocketType(jeromq.SocketType.ROUTER)
  object PULL   extends SocketType(jeromq.SocketType.PULL)
  object PUSH   extends SocketType(jeromq.SocketType.PUSH)
  object XPUB   extends SocketType(jeromq.SocketType.XPUB)
  object XSUB   extends SocketType(jeromq.SocketType.XSUB)
  object STREAM extends SocketType(jeromq.SocketType.STREAM)

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

  final class Live[A <: SocketType] private (sem: Semaphore, jSock: jeromq.ZMQ.Socket) extends Service[A] {
    override def bind(address: String): Task[Boolean]            = sem.withPermit(Task { jSock.bind(address) })
    override def close: Task[Unit]                               = sem.withPermit(Task { jSock.close() })
    override def connect(address: String): Task[Boolean]         = sem.withPermit(Task { jSock.connect(address) })
    override def receive: Task[Option[Bytes]]                    = sem.withPermit(Task { Option(jSock.recv()) })
    override def receive(flags: Int): Task[Option[Bytes]]        = sem.withPermit(Task { Option(jSock.recv(flags)) })
    override def receiveString: Task[Option[String]]             = sem.withPermit(Task { Option(jSock.recvStr()) })
    override def receiveString(flags: Int): Task[Option[String]] = sem.withPermit(Task { Option(jSock.recvStr(flags)) })
    override def send(bytes: Bytes, flags: Int): Task[Boolean]   = sem.withPermit(Task { jSock.send(bytes, flags) })
    override def send(bytes: Bytes): Task[Boolean]               = sem.withPermit(Task { jSock.send(bytes) })
    override def send(data: String, flags: Int): Task[Boolean]   = sem.withPermit(Task { jSock.send(data, flags) })
    override def send(data: String): Task[Boolean]               = sem.withPermit(Task { jSock.send(data) })
  }

  object Live {
    def open[A <: SocketType](socketType: A, context: Context.Service): ZManaged[Any, Nothing, Service[A]] =
      (for {
        semaphore <- Semaphore.make(1)
        jSocket   <- context.createSocket(socketType.jSocketType)
      } yield new Live[A](semaphore, jSocket))
        .toManaged(_.close.orDie)
        .orDie
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
