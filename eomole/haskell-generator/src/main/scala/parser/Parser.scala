package parser

import cats.data.State
import expr._

import scala.io.Source
import scala.util.matching.Regex

object Parser {
  def parse(s: String): Seq[Expr] =
    s.linesIterator.toSeq.map(_.split(' ').toSeq).map(expr.runA(_).value)

  private[this] lazy val number: Regex = "(-?\\d+)".r
  private[this] lazy val variable: Regex = ":(\\d+)".r

  type ParserState[T] = State[Seq[String], T]

  def expr: ParserState[Expr] = {
    for {
      s <- State.get
      expr <- s match {
        case (number(n)) +: rest =>
          State.set(rest).map(_ => Number(n.toLong))

        case (variable(n)) +: "=" +: rest =>
          for {
            _ <- State.set(rest)
            right <- expr
          } yield
            Let(Variable(n), right)

        case str +: "=" +: rest =>
          for {
            _ <- State.set(rest)
            right <- expr
          } yield
            Let(Variable(str), right)

        case "inc" +: rest =>
          State.set(rest).map(_ => Successor(Hole))

        case "dec" +: rest =>
          State.set(rest).map(_ => Predecessor(Hole))

        case "add" +: rest =>
          State.set(rest).map(_ => Sum(Hole, Hole))

        case (variable(n)) +: rest =>
          State.set(rest).map(_ => Variable(n))

        case "mul" +: rest =>
          State.set(rest).map(_ => Product(Hole, Hole))

        case "div" +: rest =>
          State.set(rest).map(_ => IntegerDivision(Hole, Hole))

        case "eq" +: rest =>
          State.set(rest).map(_ => BooleanEquality(Hole, Hole))

        case "lt" +: rest =>
          State.set(rest).map(_ => StrictLessThan(Hole, Hole))

        case "mod" +: rest =>
          State.set(rest).map(_ => Modulate(Hole))

        case "dem" +: rest =>
          State.set(rest).map(_ => Demodulate(Hole))

        case "send" +: rest =>
          State.set(rest).map(_ => Send(Hole))

        case "neg" +: rest =>
          State.set(rest).map(_ => Negate(Hole))

        case "ap" +: rest =>
          for {
            _ <- State.set(rest)
            f <- expr
            e <- expr
          } yield
            Apply(f, e)

        case "neg" +: rest =>
          State.set(rest).map(_ => Negate(Hole))

        case "s" +: rest =>
          State.set(rest).map(_ => S(Hole, Hole, Hole))

        case "c" +: rest =>
          State.set(rest).map(_ => C(Hole, Hole, Hole))

        case "b" +: rest =>
          State.set(rest).map(_ => B(Hole, Hole, Hole))

        case "t" +: rest =>
          State.set(rest).map(_ => True(Hole, Hole))

        case "f" +: rest =>
          State.set(rest).map(_ => False(Hole, Hole))

        case "pwr2" +: rest =>
          State.set(rest).map(_ => PowerOfTwo(Hole))

        case "i" +: rest =>
          State.set(rest).map(_ => I(Hole))

        case "cons" +: rest =>
          State.set(rest).map(_ => Cons(Hole, Hole, Hole))

        case "car" +: rest =>
          State.set(rest).map(_ => Car(Hole))

        case "cdr" +: rest =>
          State.set(rest).map(_ => Cdr(Hole))

        case "nil" +: rest =>
          State.set(rest).map(_ => Nil(Hole))

        case "isnil" +: rest =>
          State.set(rest).map(_ => IsNil(Hole))

        case "vec" +: rest =>
          State.set(rest).map(_ => Cons(Hole, Hole, Hole))

        case "draw" +: rest =>
          State.set(rest).map(_ => Draw(Hole))

        case "checkerboard" +: rest =>
          State.set(rest).map(_ => CheckerBoard(Hole, Hole))

        case "multipledraw" +: rest =>
          State.set(rest).map(_ => MultipleDraw(Hole))

        case "if0" +: rest =>
          State.set(rest).map(_ => IfZero(Hole, Hole, Hole))

        case "interact" +: rest =>
          State.set(rest).map(_ => Interact(Hole, Hole, Hole))

        case "galaxy" +: rest =>
          State.set(rest).map(_ => Galaxy())
      }
    } yield
      expr
  }

  def main(args: Array[String]): Unit =
    parse(Source.fromFile("galaxy.txt").mkString).map(println)

}
