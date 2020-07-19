package galaxy

import cats.Eval

import scala.io.Source

/**
 * @see https://message-from-space.readthedocs.io/en/latest/implementation.html
 */
class GalaxyEval(code: Seq[Expr], verbose: Boolean = false) {

  type Environment = scala.collection.mutable.Map[Variable, Eval[Expr]]

  private val env: Environment = {
    val builder = scala.collection.mutable.Map.newBuilder[Variable, Eval[Expr]]
    code.collect {
      case Let(v, e) =>
        builder.addOne(v -> Eval.later {
          val res = eval(e)
          if (verbose) println(s"${v.n} = $res")
          res
        })
    }
    builder.result()
  }

  def eval(expr: Expr): Expr = {
    expr.evaluated match {
      case Some(evaluated) => evaluated
      case _ =>
        var currentExpr: Expr = expr
        var result: Option[Expr] = None
        while (!result.contains(currentExpr)) {
          result.foreach(currentExpr = _)
          result = Some(tryEval(currentExpr))
        }
        expr.evaluated = Some(currentExpr)
        currentExpr
    }
  }

  private def tryEval(expr: Expr): Expr =
    (expr, expr.evaluated) match {
      case (_, Some(evaluated)) => evaluated
      case (v@Variable(_), _) =>
        env.getOrElse(v, throw new RuntimeException(s"${v.n} is not a variable")).value
      case (Apply(fun_, x), _) =>
        val fun = eval(fun_)
        fun match {
          case Negate => Number(-asNum(eval(x)))
          case I => x
          case Nil => True
          case IsNil => Apply(x, Apply(True, Apply(True, False)))
          case Car => Apply(x, True)
          case Cdr => Apply(x, False)
          case Apply(fun2_, y) =>
            val fun2 = eval(fun2_)
            fun2 match {
              case True => y
              case False => x
              case Sum => Number(asNum(eval(x)) + asNum(eval(y)))
              case Product => Number(asNum(eval(x)) * asNum(eval(y)))
              case IntegerDivision => Number(asNum(eval(y)) / asNum(eval(x)))
              case StrictLessThan => if (asNum(eval(y)) < asNum(eval(x))) True else False
              case BooleanEquality => if (asNum(eval(x)) == asNum(eval(y))) True else False
              case Cons => evalCons(y, x)
              case Apply(fun3_, z) =>
                val fun3 = eval(fun3_)
                fun3 match {
                  case S => Apply(Apply(z, x), Apply(y, x))
                  case C => Apply(Apply(z, x), y)
                  case B => Apply(z, Apply(y, x))
                  case Cons => Apply(Apply(x, z), y)
                  case _ => expr
                }
              case _ => expr
            }
          case _ => expr
        }
      case _ => expr
    }

  private def evalCons(a: Expr, b: Expr): Expr = {
    val res = Apply(Apply(Cons, eval(a)), eval(b))
    res.evaluated = Some(res)
    res
  }

  private def asNum(expr: Expr): Long =
    expr match {
      case Number(n) => n
      case _ => throw new RuntimeException(s"$expr is not a number.")
    }
}

object GalaxyEval {

  def toSeq(expr: Expr): Seq[Expr] =
    expr match {
      case Apply(Apply(Cons, x), xs) => x +: toSeq(xs)
      case Nil => Seq.empty
      case _ => throw new RuntimeException(s"$expr is not list")
    }

  def main(args: Array[String]): Unit = {
    val e = new GalaxyEval(Parser.parse(Source.fromFile("galaxy.txt").mkString), verbose = true)
    println(e.eval(Variable("galaxy")))
    println(toSeq(e.eval(Apply(Apply(Variable("galaxy"), Nil), Apply(Apply(Cons, Number(0)), Number(0))))))
  }
}
