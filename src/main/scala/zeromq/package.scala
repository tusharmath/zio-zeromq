import org.{zeromq => jeromq}
import zio._

package object zeromq {
  type Bytes = Array[Byte]

  type Context                        = Has[Context.Service]
  type Socket[A <: Socket.SocketType] = Has[Socket.Service[A]]
  type PAIR                           = Socket[Socket.PAIR.type]
  type PUB                            = Socket[Socket.PUB.type]
  type SUB                            = Socket[Socket.SUB.type]
  type REQ                            = Socket[Socket.REQ.type]
  type REP                            = Socket[Socket.REP.type]
  type DEALER                         = Socket[Socket.DEALER.type]
  type ROUTER                         = Socket[Socket.ROUTER.type]
  type PULL                           = Socket[Socket.PULL.type]
  type PUSH                           = Socket[Socket.PUSH.type]
  type XPUB                           = Socket[Socket.XPUB.type]
  type XSUB                           = Socket[Socket.XSUB.type]
  type STREAM                         = Socket[Socket.STREAM.type]

  lazy val PAIR   = new Socket.Type(Socket.PAIR)
  lazy val PUB    = new Socket.Type(Socket.PUB)
  lazy val SUB    = new Socket.Type(Socket.SUB)
  lazy val REQ    = new Socket.Type(Socket.REQ)
  lazy val REP    = new Socket.Type(Socket.REP)
  lazy val DEALER = new Socket.Type(Socket.DEALER)
  lazy val ROUTER = new Socket.Type(Socket.ROUTER)
  lazy val PULL   = new Socket.Type(Socket.PULL)
  lazy val PUSH   = new Socket.Type(Socket.PUSH)
  lazy val XPUB   = new Socket.Type(Socket.XPUB)
  lazy val XSUB   = new Socket.Type(Socket.XSUB)
  lazy val STREAM = new Socket.Type(Socket.STREAM)
}
