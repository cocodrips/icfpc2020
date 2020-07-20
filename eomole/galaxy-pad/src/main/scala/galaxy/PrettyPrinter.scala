package galaxy

object PrettyPrinter {
  def print(expr: Expr): String =
    expr match {
      case Number(n) => n.toString
      case Apply(Apply(Cons, Number(x)), Number(y)) => s"($x, $y)"
      case _ => toSeq(expr).map(print).mkString("[", ", ", "]")
    }

  def toSeq(expr: Expr): Seq[Expr] =
    expr match {
      case Apply(Apply(Cons, x), xs) => x +: toSeq(xs)
      case Nil => Seq.empty
      case _ => Seq(expr)
    }

  def main(args: Array[String]): Unit = {
    for (i <- -32 to 32) {
      println(i, print(Number(i)))
    }
    println(print(Nil))
    println(print(Apply(Apply(Cons, Nil), Nil)))
    println(print(Apply(Apply(Cons, Nil), Apply(Apply(Cons, Number(0)), Number(0)))))
    println(print(Apply(Apply(Cons, Number(0)), Number(0))))
  }
}
