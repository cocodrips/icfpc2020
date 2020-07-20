package galaxy

import cats.data.State
import galaxy.Modulator.mod

object Demodulator {
  def dem(s: String): Expr = one.runA(s.toCharArray).value

  type ParserState[T] = State[Seq[Char], T]

  private def one: ParserState[Expr] = {
    for {
      s <- State.get
      expr <- s match {
        case '0' +: '0' +: rest =>
          State.set(rest).map(_ => Nil)

        case '0' +: '1' +: rest =>
          for {
            _ <- State.set(rest)
            n <- number
          } yield
            Number(n)

        case '1' +: '0' +: rest =>
          for {
            _ <- State.set(rest)
            n <- number
          } yield
            Number(-n)

        case '1' +: '1' +: rest =>
          for {
            _ <- State.set(rest)
            x <- one
            xs <- one
          } yield
            Apply(Apply(Cons, x), xs)

        case _ =>
          throw new RuntimeException(s"not demodulatable text: $s")
      }
    } yield
      expr
  }

  private def number: ParserState[Long] =
    for {
      s <- State.get
      num <- {
        val hd = s.takeWhile(_ == '1').size
        val (bits, rest) = s.drop(hd + 1).splitAt(hd * 4)
        val num = bits.reverse.zipWithIndex.map({
          case ('1', d) => 1L << d
          case _ => 0
        }).reduceOption(_ | _).getOrElse(0L)
        State.set(rest).map(_ => num)
      }
    } yield
      num

  def main(args: Array[String]): Unit = {
    for (i <- -32 to 32) {
      println(i, dem(mod(Number(i))))
    }
    println(dem(mod(Nil)))
    println(dem(mod(Apply(Apply(Cons, Nil), Nil))))
    println(dem(mod(Apply(Apply(Cons, Nil), Apply(Apply(Cons, Number(0)), Number(0))))))
  }
}
