import org.{zeromq => jeromq}
import zio._

package object zeromq {
  type Bytes   = Array[Byte]
  type Context = Has[Context.Service]

  lazy val Pair   = new Socket.Type(jeromq.SocketType.PAIR)
  lazy val Pub    = new Socket.Type(jeromq.SocketType.PUB)
  lazy val Sub    = new Socket.Type(jeromq.SocketType.SUB)
  lazy val Req    = new Socket.Type(jeromq.SocketType.REQ)
  lazy val Rep    = new Socket.Type(jeromq.SocketType.REP)
  lazy val Dealer = new Socket.Type(jeromq.SocketType.DEALER)
  lazy val Router = new Socket.Type(jeromq.SocketType.ROUTER)
  lazy val Pull   = new Socket.Type(jeromq.SocketType.PULL)
  lazy val Push   = new Socket.Type(jeromq.SocketType.PUSH)
  lazy val XPub   = new Socket.Type(jeromq.SocketType.XPUB)
  lazy val XSub   = new Socket.Type(jeromq.SocketType.XSUB)
  lazy val Stream = new Socket.Type(jeromq.SocketType.STREAM)
}
