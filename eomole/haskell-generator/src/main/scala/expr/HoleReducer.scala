package expr

import parser.Parser.parse

import scala.io.Source

object HoleReducer {

  def reduce(es: Seq[Expr]): Seq[Expr] = es.map(reduce)

  def reduce(expr: Expr): Expr = expr match {

    case Let(v, e) => Let(v, reduce(e))

    case Apply(f, e) =>
      (reduce(f), reduce(e)) match {
        case (Successor(Hole), expr) => Successor(expr)
        case (Predecessor(Hole), expr) => Predecessor(expr)
        case (Sum(Hole, Hole), e1) => Sum(e1, Hole)
        case (Sum(e1, Hole), e2) => Sum(e1, e2)
        case (Product(Hole, Hole), e1) => Product(e1, Hole)
        case (Product(e1, Hole), e2) => Product(e1, e2)
        case (IntegerDivision(Hole, Hole), e1) => IntegerDivision(e1, Hole)
        case (IntegerDivision(e1, Hole), e2) => IntegerDivision(e1, e2)
        case (BooleanEquality(Hole, Hole), e1) => BooleanEquality(e1, Hole)
        case (BooleanEquality(e1, Hole), e2) => BooleanEquality(e1, e2)
        case (StrictLessThan(Hole, Hole), e1) => StrictLessThan(e1, Hole)
        case (StrictLessThan(e1, Hole), e2) => StrictLessThan(e1, e2)
        case (Modulate(Hole), expr) => Modulate(expr)
        case (Demodulate(Hole), expr) => Demodulate(expr)
        case (Send(Hole), expr) => Send(expr)
        case (Negate(Hole), expr) => Negate(expr)
        case (S(Hole, Hole, Hole), e1) => S(e1, Hole, Hole)
        case (S(e1, Hole, Hole), e2) => S(e1, e2, Hole)
        case (S(e1, e2, Hole), e3) => S(e1, e2, e3)
        case (C(Hole, Hole, Hole), e1) => C(e1, Hole, Hole)
        case (C(e1, Hole, Hole), e2) => C(e1, e2, Hole)
        case (C(e1, e2, Hole), e3) => C(e1, e2, e3)
        case (B(Hole, Hole, Hole), e1) => B(e1, Hole, Hole)
        case (B(e1, Hole, Hole), e2) => B(e1, e2, Hole)
        case (B(e1, e2, Hole), e3) => B(e1, e2, e3)
        case (True(Hole, Hole), e1) => True(e1, Hole)
        case (True(e1, Hole), e2) => True(e1, e2)
        case (False(Hole, Hole), e1) => False(e1, Hole)
        case (False(e1, Hole), e2) => False(e1, e2)
        case (PowerOfTwo(Hole), expr) => PowerOfTwo(expr)
        case (I(Hole), expr) => I(expr)
        case (Cons(Hole, Hole, Hole), e1) => Cons(e1, Hole, Hole)
        case (Cons(e1, Hole, Hole), e2) => Cons(e1, e2, Hole)
        case (Cons(e1, e2, Hole), e3) => Cons(e1, e2, e3)
        case (Car(Hole), expr) => Car(expr)
        case (Cdr(Hole), expr) => Cdr(expr)
        case (Nil(Hole), expr) => Nil(expr)
        case (IsNil(Hole), expr) => IsNil(expr)
        case (Draw(Hole), expr) => Draw(expr)
        case (CheckerBoard(Hole, Hole), e1) => CheckerBoard(e1, Hole)
        case (CheckerBoard(e1, Hole), e2) => CheckerBoard(e1, e2)
        case (MultipleDraw(Hole), expr) => MultipleDraw(expr)
        case (IfZero(Hole, Hole, Hole), e1) => IfZero(e1, Hole, Hole)
        case (IfZero(e1, Hole, Hole), e2) => IfZero(e1, e2, Hole)
        case (IfZero(e1, e2, Hole), e3) => IfZero(e1, e2, e3)
        case (Interact(Hole, Hole, Hole), e1) => Interact(e1, Hole, Hole)
        case (Interact(e1, Hole, Hole), e2) => Interact(e1, e2, Hole)
        case (Interact(e1, e2, Hole), e3) => Interact(e1, e2, e3)
        case (Hole, e) => e
        case (f, Hole) => f
        case (f, e) =>
//          println(s"消せないApply:\n\t$f\n\t$e")
          Apply(f, e)
      }
    case _ =>
      expr
  }

  def main(args: Array[String]): Unit =
    reduce(parse(Source.fromFile("galaxy.txt").mkString)).map(println)

}
