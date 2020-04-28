package zeromq

import zio.test.Assertion._
import zio.test._
import zio.test.TestAspect._
import zio.duration._
import zio._
import zio.test.environment.TestConsole

object SocketSpec extends DefaultRunnableSpec {
  private def L: ZLayer[Any, Nothing, REQ with REP] = Context.live >>> (REQ.live ++ REP.live)
  def spec =
    suite("REQ/REP")(
      testM("should bind with an address") {
        for {
          _ <- REQ.bind("ipc://tmp/ABC.sock")
        } yield assertCompletes
      }.provideSomeLayer(L),
      testM("should connect with each other") {
        checkM(MGen.inproc, MGen.inproc) { (reqA, repA) =>
          (for {
            _      <- REQ.bind(reqA)
            _      <- REP.bind(repA)
            actual <- REQ.connect(repA)
          } yield assert(actual)(isTrue)).provideSomeLayer(L)
        }
      },
      testM("should send messages") {
        checkM(MGen.inproc, Gen.anyString) { (addr, str) =>
          val rep = REP.bind(addr) *> REP.receiveString >>= (k => console.putStr(k.toString()))
          val req = REQ.connect(addr) &&& REQ.send(str)

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
