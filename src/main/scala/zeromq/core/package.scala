package zeromq

import zio.Has

package object core {
  type Bytes   = Array[Byte]
  type Context = Has[Context.Service]
  type Socket  = Has[Socket.Service]
}
