package zeromq

import zio.{Task}
import org.{zeromq => jeromq}
import zio.ZManaged
import zio.ZLayer

object Context {
  type ManagedContext = ZManaged[Any, Nothing, Service]
  type ZLayerContext  = ZLayer[Any, Nothing, Context]

  trait Service {
    def createSocket(socketType: jeromq.SocketType): Task[jeromq.ZMQ.Socket]
    def close: Task[Unit]
  }

  final class Live private (jContext: jeromq.ZContext) extends Service {

    override def createSocket(socketType: jeromq.SocketType): Task[jeromq.ZMQ.Socket] = Task {
      jContext.createSocket(socketType)
    }

    override def close: Task[Unit] = Task {
      jContext.close()
    }
  }

  object Live {
    def open: ManagedContext                                 = open(Task { new jeromq.ZContext() })
    def open(context: Task[jeromq.ZContext]): ManagedContext = context.map(new Live(_)).toManaged(_.close.orDie).orDie
    def open(ioThreads: Int): ManagedContext                 = open(Task { new jeromq.ZContext(ioThreads) })
  }

  def live: ZLayerContext                                 = Live.open.toLayer
  def live(context: Task[jeromq.ZContext]): ZLayerContext = Live.open(context).toLayer
  def live(ioThreads: Int): ZLayerContext                 = Live.open(ioThreads).toLayer
}
