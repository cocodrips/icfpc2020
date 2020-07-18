package expr

import scala.annotation.tailrec

sealed trait Expr {
  def isFilled: Boolean
}

case object Hole extends Expr {
  override def isFilled: Boolean = false
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/radio-transmission-recording.html
 */
case class Number(n: Long) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message4.html
 */
case class Let(v: Variable, e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message5.html
 */
case class Successor(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message6.html
 */
case class Predecessor(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message7.html
 */
case class Sum(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message8.html
 */
case class Variable(n: String) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message9.html
 */
case class Product(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message10.html
 */
case class IntegerDivision(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message11.html
 */
case class BooleanEquality(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message12.html
 */
case class StrictLessThan(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message13.html
 * @see https://message-from-space.readthedocs.io/en/latest/message35.html
 */
case class Modulate(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

case class BitString(s: String) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message14.html
 */
case class Demodulate(m: Expr) extends Expr {
  override def isFilled: Boolean = m != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message15.html
 * @see https://message-from-space.readthedocs.io/en/latest/message36.html
 */
case class Send(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message16.html
 */
case class Negate(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message17.html
 */
case class Apply(f: Expr, e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message18.html
 */
case class S(e1: Expr, e2: Expr, e3: Expr) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message19.html
 */
case class C(e1: Expr, e2: Expr, e3: Expr) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message20.html
 */
case class B(e1: Expr, e2: Expr, e3: Expr) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message21.html
 */
case class True(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message22.html
 */
case class False(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = true
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message23.html
 */
case class PowerOfTwo(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message24.html
 */
case class I(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message25.html
 */
case class Cons(e1: Expr, e2: Expr, f: Expr) extends Expr {
  def toSeq: Seq[Expr] = toSeq(Seq.newBuilder)

  @tailrec private def toSeq(builder: scala.collection.mutable.Builder[Expr, Seq[Expr]]): Seq[Expr] = {
    builder.addOne(e1)
    e2 match {
      case c: Cons => c.toSeq(builder)
      case _: Nil => builder.result()
    }
  }

  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message26.html
 */
case class Car(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message27.html
 */
case class Cdr(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message28.html
 */
case class Nil(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message29.html
 */
case class IsNil(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message31.html
 */
case class Vec(n1: Number, n2: Number) extends Expr {
  override def isFilled: Boolean = true
}

object Vec {
  def of(e: Expr): Option[Vec] =
    e match {
      case Cons(n1: Number, n2: Number, _) => Some(Vec(n1, n2))
      case _ => None
    }
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message32.html
 */
case class Draw(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

//case class Draw(vs: Seq[Vec]) extends Expr
//
//object Draw extends Expr {
//  def of(e: Expr): Option[Draw] =
//    e match {
//      case c: Cons => Some(Draw(c.toSeq.flatMap(Vec.of)))
//      case _: Nil => Some(Draw(Seq.empty))
//      case _ => None
//    }
//}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message33.html
 */
case class CheckerBoard(e1: Expr, e2: Expr) extends Expr {
  override def isFilled: Boolean = e2 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message34.html
 */
case class MultipleDraw(e: Expr) extends Expr {
  override def isFilled: Boolean = e != Hole
}

//case class MultipleDraw(vss: Seq[Seq[Vec]]) extends Expr
//
//object MultipleDraw {
//  def of(e: Expr): Option[MultipleDraw] =
//    e match {
//      case c: Cons => Some(MultipleDraw(c.toSeq.flatMap(Draw.of).map(_.vs)))
//      case _: Nil => Some(MultipleDraw(Seq.empty))
//      case _ => None
//    }
//}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message37.html
 */
case class IfZero(e: Expr, t: Expr, f: Expr) extends Expr {
  override def isFilled: Boolean = f != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message38.html
 */
case class Interact(e1: Expr, e2: Expr, e3: Expr) extends Expr {
  override def isFilled: Boolean = e3 != Hole
}

/**
 * @see https://message-from-space.readthedocs.io/en/latest/message42.html
 */
case class Galaxy() extends Expr {
  override def isFilled: Boolean = true
}

case object EvalError extends Expr {
  override def isFilled: Boolean = true
}
