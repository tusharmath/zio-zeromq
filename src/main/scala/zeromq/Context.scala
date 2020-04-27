package zeromq

import zio.{Task, UIO}
import org.{zeromq => jeromq}
import zio.ZManaged
import zio.ZLayer

object Context {
  trait Service {
    def createSocket(socketType: jeromq.SocketType): UIO[jeromq.ZMQ.Socket]
    def close: Task[Unit]
  }

  final class Live private (jContext: jeromq.ZContext) extends Service {

    override def createSocket(socketType: jeromq.SocketType): UIO[jeromq.ZMQ.Socket] = UIO {
      jContext.createSocket(socketType)
    }

    override def close: Task[Unit] = Task {
      jContext.close()
    }
  }

  object Live {
    def open: ZManaged[Any, Throwable, Service] = open(new jeromq.ZContext())
    def open(context: jeromq.ZContext): ZManaged[Any, Throwable, Service] =
      UIO(new Live(context)).toManaged(_.close.orDie)
    def open(ioThreads: Int): ZManaged[Any, Throwable, Service] = open(new jeromq.ZContext(ioThreads))
  }

  def live: ZLayer[Any, Throwable, Context]                           = Live.open.toLayer
  def live(context: jeromq.ZContext): ZLayer[Any, Throwable, Context] = Live.open(context).toLayer
  def live(ioThreads: Int): ZLayer[Any, Throwable, Context]           = Live.open(ioThreads).toLayer
}
