# ZIO ZeroMQ

A ZIO-based interface to [ZeroMQ].

[ZeroMQ]: https://zeromq.org

## Usage

```scala
import zio._
import zeromq._

// Create a communication port
val address = "inproc://1"

// Create REQ and REP sockets
val req     = REQ.connect(address) *> REQ.send("Hello World!")
val rep     = REP.bind(address) *> REP.receiveString >>= console.putStrLn(_) // prints "Hello World!"


// Create Program
val program = (rep <&> req).provideSomeLayer {
  Context >>> (REP.live ++ REQ.live)
}
```
