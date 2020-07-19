package galaxy

import cats.data.State

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
            Let(Variable(s"x$n"), right)

        case str +: "=" +: rest =>
          for {
            _ <- State.set(rest)
            right <- expr
          } yield
            Let(Variable(str), right)

        case Successor.label +: rest =>
          State.set(rest).map(_ => Successor)

        case Predecessor.label +: rest =>
          State.set(rest).map(_ => Predecessor)

        case Sum.label +: rest =>
          State.set(rest).map(_ => Sum)

        case (variable(n)) +: rest =>
          State.set(rest).map(_ => Variable(s"x$n"))

        case Product.label +: rest =>
          State.set(rest).map(_ => Product)

        case IntegerDivision.label +: rest =>
          State.set(rest).map(_ => IntegerDivision)

        case BooleanEquality.label +: rest =>
          State.set(rest).map(_ => BooleanEquality)

        case StrictLessThan.label +: rest =>
          State.set(rest).map(_ => StrictLessThan)

        case Modulate.label +: rest =>
          State.set(rest).map(_ => Modulate)

        case Demodulate.label +: rest =>
          State.set(rest).map(_ => Demodulate)

        case Send.label +: rest =>
          State.set(rest).map(_ => Send)

        case Negate.label +: rest =>
          State.set(rest).map(_ => Negate)

        case "ap" +: rest =>
          for {
            _ <- State.set(rest)
            f <- expr
            e <- expr
          } yield
            Apply(f, e)

        case Negate.label +: rest =>
          State.set(rest).map(_ => Negate)

        case S.label +: rest =>
          State.set(rest).map(_ => S)

        case C.label +: rest =>
          State.set(rest).map(_ => C)

        case B.label +: rest =>
          State.set(rest).map(_ => B)

        case True.label +: rest =>
          State.set(rest).map(_ => True)

        case False.label +: rest =>
          State.set(rest).map(_ => False)

        case PowerOfTwo.label +: rest =>
          State.set(rest).map(_ => PowerOfTwo)

        case I.label +: rest =>
          State.set(rest).map(_ => I)

        case Cons.label +: rest =>
          State.set(rest).map(_ => Cons)

        case Car.label +: rest =>
          State.set(rest).map(_ => Car)

        case Cdr.label +: rest =>
          State.set(rest).map(_ => Cdr)

        case Nil.label +: rest =>
          State.set(rest).map(_ => Nil)

        case IsNil.label +: rest =>
          State.set(rest).map(_ => IsNil)

        case Vec.label +: rest =>
          State.set(rest).map(_ => Cons)

        case Draw.label +: rest =>
          State.set(rest).map(_ => Draw)

        case CheckerBoard.label +: rest =>
          State.set(rest).map(_ => CheckerBoard)

        case MultipleDraw.label +: rest =>
          State.set(rest).map(_ => MultipleDraw)

        case IfZero.label +: rest =>
          State.set(rest).map(_ => IfZero)

        case Interact.label +: rest =>
          State.set(rest).map(_ => Interact)

        case Galaxy.label +: rest =>
          State.set(rest).map(_ => Galaxy)
      }
    } yield
      expr
  }
}
