package galaxy

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message13.html
 * @see https://message-from-space.readthedocs.io/en/latest/message35.html
 */
object Modulator {
  def mod(expr: Expr): String =
    expr match {
      case Number(n) if n >= 0 => s"01${modulateNum(n)}"
      case Number(n) => s"10${modulateNum(-n)}"
      case Nil => s"00"
      case Apply(Apply(Cons, x), xs) => s"11${mod(x)}${mod(xs)}"
      case _ => throw new RuntimeException(s"not modulatable expr: $expr")
    }

  private def modulateNum(n: Long): String = {
    val hexadecimals = LazyList.iterate(n)(_ >> 4).takeWhile(_ > 0).size
    val body = Seq.tabulate(hexadecimals * 4)(d => Math.min(n & 1L << d, 1)).reverse.mkString
    s"${"1" * hexadecimals}0$body"
  }

  def main(args: Array[String]): Unit = {
    for (i <- -32 to 32) {
      println(i, mod(Number(i)))
    }
    println(mod(Nil))
    println(mod(Apply(Apply(Cons, Nil), Nil)))
    println(mod(Apply(Apply(Cons, Nil), Apply(Apply(Cons, Number(0)), Number(0)))))
  }
}
