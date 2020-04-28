package zeromq

import zio.test.Assertion._
import zio.test._
import zio.test.TestAspect._
import zio.duration._
import zio._
import zio.test.environment.TestConsole

object SocketSpec extends DefaultRunnableSpec {
  private def L = Context.live >>> (Req.live ++ Rep.live)
  def spec =
    suite("Socket")(
      suite("bind()") {
        testM("should bind with an address") {
          for {
            _ <- Req.bind("ipc://tmp/ABC.sock")
          } yield assertCompletes
        }.provideCustomLayer(L)
      } @@ ignore,
      suite("REQ/REP")(
        testM("should connect with each other") {
          checkM(MGen.inproc, MGen.inproc) { (reqA, repA) =>
            (for {
              _      <- Req.bind(reqA)
              _      <- Rep.bind(repA)
              actual <- Req.connect(repA)
            } yield assert(actual)(isTrue)).provideCustomLayer(L)
          }
        } @@ ignore,
        testM("should send messages") {
          checkM(MGen.inproc, Gen.alphaNumericStringBounded(2, 10)) { (addr, str) =>
            val rep = Rep.bind(addr) *> Rep.receiveString >>= (k => console.putStrLn(k.toString()))
            val req = Req.connect(addr) &&& Req.send(str)

            for {
              _        <- (rep <&> req).provideCustomLayer(L)
              actual   <- TestConsole.output
              expected = List(Option(str).toString())
              _        <- TestConsole.clearOutput
            } yield assert(actual.toList)(equalTo(expected))
          }
        }
      )
    )
}
