package zeromq

import zio.test._
import zio.test.Assertion._
import zio.UIO
import zio.Has
import zio.ZLayer
import zio.RIO
import zio.ZManaged

object SocketSpec extends DefaultRunnableSpec {
  def spec = suite("Req") {
    testM("should bind with an address") {
      (for {
        _ <- Req.bind("inproc://ABC")
      } yield assertCompletes).provideLayer(Context.live >>> Req.live)
    }
  }
}
