package galaxy

import java.util.Scanner

import scala.annotation.tailrec
import scala.io.Source

class GalaxyInteract(eval: GalaxyEval) {

  def toSeq(expr: Expr): Seq[Expr] =
    expr match {
      case Apply(Apply(Cons, x), xs) => x +: toSeq(xs)
      case Nil => Seq.empty
      case _ => throw new RuntimeException(s"$expr is not list")
    }

  def toVec(expr: Expr): (Long, Long) =
    expr match {
      case Apply(Apply(Cons, Number(x)), Number(y)) => (x, y)
      case _ => throw new RuntimeException(s"$expr is not vec")
    }

  def toState(expr: Expr): Seq[Any] =
    toSeq(expr).map {
      case Number(n) => n
      case Apply(Apply(Cons, x), xs) => x +: toSeq(xs)
      case Nil => Seq.empty
      case e => e
    }

  def toPitures(expr: Expr): Seq[Seq[(Long, Long)]] =
    toSeq(expr).map(toSeq(_).map(toVec))

  def toVisualizable(flag: Int, newState: Expr, data: Expr): String = {
    val Seq(Number(a), b, Number(c), d) = toSeq(newState)
    val st = Seq(a, toSeq(b).map({ case Number(n) => n }).mkString("[", ",", "]"), c, toSeq(d).mkString("[", ",", "]")).mkString("[", ",", "]")
    val da = toPitures(data).map(_.mkString("[", ",", "]")).mkString("[", ",", "]")
    s"[$flag,$st,$da]"
  }

  def start(state: Expr = Nil): Unit = interact(state, click())

  // create cache
  eval.eval(Apply(Apply(Variable("galaxy"), Nil), Apply(Apply(Cons, Number(0)), Number(0))))

  @tailrec private def interact(state: Expr, event: Expr): (Expr, Expr) = {
    val expr = Apply(Apply(Variable("galaxy"), state), event)
    val Seq(flag, newState, data) = toSeq(eval.eval(expr))

    flag match {
      case Number(0) =>
        println("my turn")
        println("newState", newState)
        println(Modulator.mod(newState))
        drawPictures(toPitures(data))
        println()
        interact(newState, click())
      case _ =>
        println("enemy's turn")
        println("newState", newState)
        println("sendData", data)
        val response = sendAlienProxy(data)
        println("response", response)
        interact(newState, response)
    }
  }

  private lazy val sc = new Scanner(System.in)

  def crossPoints(vecs: Seq[(Long, Long)]): Seq[(Long, Long)] =
    vecs.sorted.groupBy(identity).toSeq.filter(_._2.size > 1).sortBy(_._2.size).reverse.map(_._1)

  def click(): Expr = {
    println(s"input click pos:")
    val x = sc.nextInt()
    val y = sc.nextInt()
    Apply(Apply(Cons, Number(x)), Number(y))
  }

  def sendAlienProxy(data: Expr): Expr = {
    println(s"send alien proxy: `${Modulator.mod(data)}`")
    println(s"input response:")
    val response = getUserInput()
    Demodulator.dem(response)
  }

  @tailrec private def getUserInput(): String = {
    val response = sc.next()
    if (response.isEmpty) {
      getUserInput()
    } else {
      response
    }
  }

  def drawPicture(picture: Seq[(Long, Long)]): Unit =
    if (picture.nonEmpty) {
      val (xs, ys) = picture.unzip
      val minx = xs.min
      val maxx = xs.max
      val dx = (maxx - minx + 1).toInt
      val miny = ys.min
      val maxy = ys.max
      val dy = (maxy - miny + 1).toInt
      println(s"${(minx, miny)} -> ${(maxx, maxy)}")
      println("cross points", crossPoints(picture))
      val picStr = Array.fill(dy, dx)('.')
      picture.foreach { case (x, y) =>
        picStr((y - miny).toInt)((x - minx).toInt) = '@'
      }
      picStr.foreach(line => println(new String(line)))
    }

  def getCorners(picture: Seq[(Long, Long)]): Option[((Long, Long), (Long, Long))] =
    if (picture.nonEmpty) {
      val (xs, ys) = picture.unzip
      val minx = xs.min
      val maxx = xs.max
      val miny = ys.min
      val maxy = ys.max
      Some(((minx, miny), (maxx, maxy)))
    } else {
      None
    }

  def drawPictures(pictures: Seq[Seq[(Long, Long)]]): Unit = {
    val (min, max) = pictures.flatMap(getCorners).unzip
    val (minxs, minys) = min.unzip
    val (maxxs, maxys) = max.unzip
    val minx = minxs.min
    val miny = minys.min
    val maxx = maxxs.max
    val maxy = maxys.max
    val dx = (maxx - minx + 1).toInt
    val dy = (maxy - miny + 1).toInt
    println(s"${(minx, miny)} -> ${(maxx, maxy)}")
    println("cross points", crossPoints(pictures.flatten))
    val picStr = Array.fill(dy, dx)('.')
    pictures.zipWithIndex.foreach { case (picture, idx) =>
      picture.foreach { case (x, y) =>
        if (picStr((y - miny).toInt)((x - minx).toInt) != '.') {
          picStr((y - miny).toInt)((x - minx).toInt) = '@'
        } else {
          picStr((y - miny).toInt)((x - minx).toInt) = ('0' + idx).asInstanceOf[Char]
        }
      }
    }
    picStr.foreach(line => println(new String(line)))
  }
}

object Main {
  val LargeGalaxy =
    Apply(Apply(Cons, Number(2)), Apply(Apply(Cons, Apply(Apply(Cons, Number(1)), Apply(Apply(Cons, Number(-1)), Nil))), Apply(Apply(Cons, Number(0)), Apply(Apply(Cons, Nil), Nil))))

  val ChoosePlayMode =
    Apply(Apply(Cons,Number(5)),Apply(Apply(Cons,Apply(Apply(Cons,Number(2)),Apply(Apply(Cons,Number(0)),Apply(Apply(Cons,Nil),Apply(Apply(Cons,Nil),Apply(Apply(Cons,Nil),Apply(Apply(Cons,Nil),Apply(Apply(Cons,Nil),Apply(Apply(Cons,Number(29651)),Nil))))))))),Apply(Apply(Cons,Number(8)),Apply(Apply(Cons,Nil),Nil))))

  def main(args: Array[String]): Unit =
    new GalaxyInteract(new GalaxyEval(Parser.parse(Source.fromFile("galaxy.txt").mkString))).start(PlaySingleMode)
}
