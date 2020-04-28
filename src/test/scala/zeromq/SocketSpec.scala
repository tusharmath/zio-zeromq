package zeromq

import zio.test.Assertion._
import zio.test._
import zio.test.TestAspect._
import zio.duration._
import zio._
import zio.test.environment.TestConsole

object SocketSpec extends DefaultRunnableSpec {
  private def L: ZLayer[Any, Nothing, ReqSocket with RepSocket] = Context.live >>> (Req.live ++ Rep.live)
  def spec =
    suite("REQ/REP")(
      testM("should bind with an address") {
        for {
          _ <- Req.bind("ipc://tmp/ABC.sock")
        } yield assertCompletes
      }.provideSomeLayer(L),
      testM("should connect with each other") {
        checkM(MGen.inproc, MGen.inproc) { (reqA, repA) =>
          (for {
            _      <- Req.bind(reqA)
            _      <- Rep.bind(repA)
            actual <- Req.connect(repA)
          } yield assert(actual)(isTrue)).provideSomeLayer(L)
        }
      },
      testM("should send messages") {
        checkM(MGen.inproc, Gen.anyString) { (addr, str) =>
          val rep = Rep.bind(addr) *> Rep.receiveString >>= (k => console.putStr(k.toString()))
          val req = Req.connect(addr) &&& Req.send(str)

          (for {
            _        <- rep <&> req
            actual   <- TestConsole.output
            expected = Vector(Option(str).toString())
            _        <- TestConsole.clearOutput
          } yield assert(actual)(equalTo(expected))).provideSomeLayer[ZTestEnv with ZEnv](L)
        }
      }
    ) @@ silent @@ timeout(5 second)
}
