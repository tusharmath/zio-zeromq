package zeromq

import zio.random.Random
import zio.test.Gen

object MGen {
  def inproc: Gen[Random, String] =
    for {
      str <- Gen.anyUUID
    } yield s"inproc://${str.toString}"
}
