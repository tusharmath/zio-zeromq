import org.{zeromq => jeromq}
import zio._

package object zeromq {
  type Bytes                          = Array[Byte]
  type Context                        = Has[Context.Service]
  type Socket[A <: Socket.SocketType] = Has[Socket.Service[A]]

  lazy val Pair   = new Socket.Type(Socket.Pair)
  lazy val Pub    = new Socket.Type(Socket.Pub)
  lazy val Sub    = new Socket.Type(Socket.Sub)
  lazy val Req    = new Socket.Type(Socket.Req)
  lazy val Rep    = new Socket.Type(Socket.Rep)
  lazy val Dealer = new Socket.Type(Socket.Dealer)
  lazy val Router = new Socket.Type(Socket.Router)
  lazy val Pull   = new Socket.Type(Socket.Pull)
  lazy val Push   = new Socket.Type(Socket.Push)
  lazy val XPub   = new Socket.Type(Socket.XPub)
  lazy val XSub   = new Socket.Type(Socket.XSub)
  lazy val Stream = new Socket.Type(Socket.Stream)
}
