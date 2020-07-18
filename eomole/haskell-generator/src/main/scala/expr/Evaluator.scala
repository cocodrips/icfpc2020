package expr

import java.util.UUID

import cats.Eval
import cats.data.State
import expr.HoleReducer.reduce
import parser.Parser.parse

import scala.io.Source

class Evaluator(es: Seq[Expr]) {

  type Environment = scala.collection.mutable.Map[Variable, Eval[Expr]]
  type EvaluatorState[T] = State[Map[Variable, Expr], T]

  private val env: Environment = {
    val builder = scala.collection.mutable.Map.newBuilder[Variable, Eval[Expr]]
    es.collect {
      case Let(v, e) =>
        builder.addOne(v -> Eval.later {
          println(s"${v.n}がリクエストされた: ${reduce(e)}")
          val res = evaluate(reduce(e))
          println(s"${v.n} -> $res")
          res
        })
    }
    builder.result()
  }

  def get(v: Variable): Option[Expr] = env.get(v).map(_.value)

  def eval(expr: Expr): Expr = evaluate(reduce(expr))

  private def evaluate(expr: Expr): Expr = expr match {
    case Successor(expr) =>
      evaluate(expr) match {
        case Number(n) => Number(n + 1)
        case e => Successor(e)
      }
    case Predecessor(expr) =>
      evaluate(expr) match {
        case Number(n) => Number(n - 1)
        case e => Predecessor(e)
      }
    case Sum(e1, e2) =>
      (evaluate(e1), evaluate(e2)) match {
        case (Number(n1), Number(n2)) => Number(n1 + n2)
        case (e1, e2) => Sum(e1, e2)
      }
    case Product(e1, e2) =>
      (evaluate(e1), evaluate(e2)) match {
        case (Number(n1), Number(n2)) => Number(n1 * n2)
        case (e1, e2) => Product(e1, e2)
      }
    case IntegerDivision(e1, e2) =>
      (evaluate(e1), evaluate(e2)) match {
        case (Number(n1), Number(n2)) => Number(n1 / n2)
        case (e1, e2) => IntegerDivision(e1, e2)
      }
    case BooleanEquality(e1, e2) =>
      (evaluate(e1), evaluate(e2)) match {
        case (Number(n1), Number(n2)) => if (n1 == n2) True(Hole, Hole) else False(Hole, Hole)
        case (e1, e2) => BooleanEquality(e1, e2)
      }
    case StrictLessThan(e1, e2) =>
      (evaluate(e1), evaluate(e2)) match {
        case (Number(n1), Number(n2)) => if (n1 < n2) True(Hole, Hole) else False(Hole, Hole)
        case (e1, e2) => StrictLessThan(e1, e2)
      }
    //    case Modulate(e) =>
    //    case Demodulate(e) =>
    //    case Send(e) =>
    case Negate(expr) =>
      evaluate(expr) match {
        case Number(n) => Number(-n)
        case e => Negate(e)
      }
    case Apply(f, e) =>
      val r = reduce(Apply(evaluate(f), e))
      println(r)
      r
    case S(x0, x1, x2) =>
      val x2_ = Variable(UUID.randomUUID().toString)
      env.addOne(x2_ -> Eval.later {
        println(s"${x2_.n}がリクエストされた: $x2")
        val res = evaluate(x2)
        println(s"${x2_.n} -> $res")
        res
      })
      evaluate(reduce(Apply(Apply(x0, x2_), Apply(x1, x2_))))
    case C(x0, x1, x2) =>
      evaluate(reduce(Apply(Apply(x0, x2), x1)))
    case B(x0, x1, x2) =>
      evaluate(reduce(Apply(x0, Apply(x1, x2))))
    case True(x0, _) =>
      evaluate(x0)
    case False(_, x1) =>
      evaluate(x1)
    case PowerOfTwo(expr) =>
      evaluate(expr) match {
        case Number(n) if n >= 0 => Number(BigInt(2).pow(n.toInt).longValue)
        case e => PowerOfTwo(e)
      }
    case I(x0) =>
      evaluate(x0)
    case Cons(x0, x1, x2) =>
      evaluate(reduce(Apply(Apply(x2, x0), x1)))
    case Car(e) =>
      evaluate(e) match {
        case Cons(x0, _, Hole) => evaluate(x0)
        case e => evaluate(reduce(Apply(e, True(Hole, Hole))))
      }
    case Cdr(e) =>
      evaluate(e) match {
        case Cons(_, x1, Hole) => evaluate(x1)
        case e => evaluate(reduce(Apply(e, False(Hole, Hole))))
      }
    case Nil(_) =>
      True(Hole, Hole)
    case IsNil(e) =>
      evaluate(e) match {
        case Nil(_) => True(Hole, Hole)
        case Cons(_, _, _) => False(Hole, Hole)
        case _ => IsNil(e)
      }
    //    case Draw(e) =>
    //    case CheckerBoard(e) =>
    //    case MultipleDraw(e) =>
    case IfZero(x0, x1, x2) =>
      evaluate(x0) match {
        case Number(0) => evaluate(x1)
        case Number(_) => evaluate(x2)
        case e => IfZero(e, x1, x2)
      }
    //    case Interact(e1, e2, e3) =>

    case v: Variable =>
      env.getOrElse(v, Eval.now(EvalError)).value
    case _ =>
//      println(s"not evalutable: $expr")
      expr
  }

}

object Evaluator {
  def main(args: Array[String]): Unit = {
    val e = new Evaluator(parse(Source.fromFile("galaxy.txt").mkString))
    println(e.get(Variable("galaxy")))
    println(e.eval(Apply(Variable("galaxy"), Nil(Hole))))
    println(e.eval(Apply(Apply(Variable("galaxy"), Nil(Hole)), Cons(Number(0), Number(0), Hole))))
  }
}
