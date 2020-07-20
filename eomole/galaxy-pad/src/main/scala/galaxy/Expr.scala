package galaxy

sealed abstract class Expr(val label: String) {
  var evaluated: Option[Expr] = None
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/radio-transmission-recording.html
 */
case class Number(n: Long) extends Expr(n.toString)

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message4.html
 */
case class Let(v: Variable, e: Expr) extends Expr("=")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message5.html
 */
case object Successor extends Expr("inc")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message6.html
 */
case object Predecessor extends Expr("dec")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message7.html
 */
case object Sum extends Expr("add")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message8.html
 */
case class Variable(n: String) extends Expr(n)

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message9.html
 */
case object Product extends Expr("mul")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message10.html
 */
case object IntegerDivision extends Expr("div")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message11.html
 */
case object BooleanEquality extends Expr("eq")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message12.html
 */
case object StrictLessThan extends Expr("lt")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message13.html
 * @see https://message-from-space.readthedocs.io/en/latest/message35.html
 */
case object Modulate extends Expr("mod")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message14.html
 */
case object Demodulate extends Expr("dem")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message15.html
 * @see https://message-from-space.readthedocs.io/en/latest/message36.html
 */
case object Send extends Expr("send")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message16.html
 */
case object Negate extends Expr("neg")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message17.html
 */
case class Apply(f: Expr, e: Expr) extends Expr("ap")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message18.html
 */
case object S extends Expr("s")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message19.html
 */
case object C extends Expr("c")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message20.html
 */
case object B extends Expr("b")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message21.html
 */
case object True extends Expr("t")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message22.html
 */
case object False extends Expr("f")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message23.html
 */
case object PowerOfTwo extends Expr("pwr2")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message24.html
 */
case object I extends Expr("i")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message25.html
 */
case object Cons extends Expr("cons")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message26.html
 */
case object Car extends Expr("car")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message27.html
 */
case object Cdr extends Expr("cdr")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message28.html
 */
case object Nil extends Expr("nil")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message29.html
 */
case object IsNil extends Expr("isnil")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message31.html
 */
case object Vec extends Expr("vec")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message32.html
 */
case object Draw extends Expr("draw")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message33.html
 */
case object CheckerBoard extends Expr("checkerboard")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message34.html
 */
case object MultipleDraw extends Expr("multipledraw")


/**
 * @see https://message-from-space.readthedocs.io/en/latest/message37.html
 */
case object IfZero extends Expr("if0")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message38.html
 */
case object Interact extends Expr("interact")

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message42.html
 */
case object Galaxy extends Expr("galaxy")
