package zeromq

import org.{zeromq => jeromq}
import zio.{Semaphore, Task, ZLayer, ZManaged}

object Context {
  type ManagedContext = ZManaged[Any, Nothing, Service]
  type ZLayerContext  = ZLayer[Any, Nothing, Context]

  trait Service {
    def createSocket(socketType: jeromq.SocketType): Task[jeromq.ZMQ.Socket]
    def close: Task[Unit]
  }

  final class Live private (sem: Semaphore, jCont: jeromq.ZContext) extends Service {
    override def createSocket(socketType: jeromq.SocketType): Task[jeromq.ZMQ.Socket] =
      sem.withPermit(Task { jCont.createSocket(socketType) })

    override def close: Task[Unit] = sem.withPermit(Task { jCont.close() })
  }

  object Live {
    def open: ManagedContext = open(Task { new jeromq.ZContext() })
    def open(contextM: Task[jeromq.ZContext]): ManagedContext =
      (for {
        semaphore <- Semaphore.make(1)
        context   <- contextM
      } yield new Live(semaphore, context)).toManaged(_.close.orDie).orDie
    def open(ioThreads: Int): ManagedContext = open(Task { new jeromq.ZContext(ioThreads) })
  }

  def live: ZLayerContext                                 = Live.open.toLayer
  def live(context: Task[jeromq.ZContext]): ZLayerContext = Live.open(context).toLayer
  def live(ioThreads: Int): ZLayerContext                 = Live.open(ioThreads).toLayer
}
