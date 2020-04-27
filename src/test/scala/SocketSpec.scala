package zeromq

import zio.test._
import zio.test.Assertion._

object SocketSpec extends DefaultRunnableSpec {
  def spec = suite("Req/Rep") {
    testM("should connect with each other") {
      (for {
        _        <- Req.send("ABC")
        actual   <- Rep.receiveString
        expected = "ABC"
      } yield assert(actual)(isSome(equalTo(expected)))).provideSomeLayer(Context.live >>> Req.live ++ Rep.live)
    }
  }
}
