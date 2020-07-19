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

  def toPitures(expr: Expr): Seq[Seq[(Long, Long)]] =
    toSeq(expr).map(toSeq(_).map(toVec))

  // create cache
  eval.eval(Apply(Apply(Variable("galaxy"), Nil), Apply(Apply(Cons, Number(0)), Number(0))))

  def start(state: Expr, event: Expr): Unit = interact(state, event)

  @tailrec private def interact(state: Expr, event: Expr): Unit = {
    println("==== ==== ==== ====")
    println(s"resume password: ${Modulator.mod(state)} ${Modulator.mod(event)}")

    val expr = Apply(Apply(Variable("galaxy"), state), event)
    val Seq(flag, newState, data) = toSeq(eval.eval(expr))

    flag match {
      case Number(0) =>
        println("user input:")
        println(s"\tnewState: ${PrettyPrinter.print(newState)}")
        drawPictures(toPitures(data))
        println()
        interact(newState, GalaxyInteract.click())
      case _ =>
        println("send to server:")
        println(s"\tnewState: ${PrettyPrinter.print(newState)}")
        println(s"\tsending data ${PrettyPrinter.print(data)}")
        val response = GalaxyInteract.sendAlienProxy(data)
        println(s"\tresponse: ${PrettyPrinter.print(response)}")
        interact(newState, response)
    }
  }

  def crossPoints(vecs: Seq[(Long, Long)]): Seq[(Long, Long)] =
    vecs.sorted.groupBy(identity).toSeq.filter(_._2.size > 1).sortBy(_._2.size).reverse.map(_._1)

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
    println()
    println(s"upper left corner: ${(minx, miny)}")
    println(s"lower right corner: ${(maxx, maxy)}")
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

object GalaxyInteract {

  private lazy val sc = new Scanner(System.in)

  def click(): Expr = {
    println(s"input click pos:")
    val x = sc.nextInt()
    val y = sc.nextInt()
    Apply(Apply(Cons, Number(x)), Number(y))
  }

  def sendAlienProxy(data: Expr): Expr = {
    println(s"send to alien proxy (https://icfpc2020-api.testkontur.ru/swagger/index.html)")
    println(s"\t${Modulator.mod(data)}")
    println(s"paste response from proxy:")
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

  def main(args: Array[String]): Unit = {
    println("usege:")
    println("\t`java -Xss1g -jar galaxy.jar [modulated state str] [modulated event str]`")
    println("\tfrom init: `java -Xss1g -jar galaxy.jar 00 11010010`")
    println("\tfrom large galaxy: `java -Xss1g -jar galaxy.jar 11011000011111011010110011010110000 110110000101100100`")
    println("\tfrom select (a): `java -Xss1g -jar galaxy.jar 110110010111110110000111010110011001100110011001101111100110010011011001001101101000110000 1101100001110111110011001001101100100`")
    println("\tfrom select (b): `java -Xss1g -jar galaxy.jar 110110011011110110000111011001111101111111111111111001111110100011011010111111100000100001100111100001010010011011011000011101011010110011001101100100110011110110100011011000011111011110000111000000110110000111011100100000000110011000011001100001101101000110000 1101100001110110000111110110100011011000011111011110000111000000110110000111011100100000000110011000011110101100111111110110000111010111101110000100000101111011000010101111010110101101011011000010011010110111001000000110110000100110000000000`")
    println()

    val initState = if (args.size >= 1) Demodulator.dem(args(0)) else Nil
    val initEvent = if (args.size >= 2) Demodulator.dem(args(1)) else click()

    new GalaxyInteract(new GalaxyEval(Parser.parse(Source.fromURL(getClass.getResource("/galaxy.txt")).mkString)))
      .start(initState, initEvent)
  }
}
