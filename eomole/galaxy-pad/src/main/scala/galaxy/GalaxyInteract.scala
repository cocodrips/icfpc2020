package galaxy

import java.nio.charset.StandardCharsets
import java.util.Scanner

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.Source

class GalaxyInteract(eval: GalaxyEval, verbose: Boolean = false) {

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
    if (verbose) {
      println("==== ==== ==== ====")
      println(s"resume password: ${Modulator.mod(state)} ${Modulator.mod(event)}")
    }

    val expr = Apply(Apply(Variable("galaxy"), state), event)
    val Seq(flag, newState, data) = toSeq(eval.eval(expr))

    flag match {
      case Number(0) =>
        if (verbose) {
          println("user input:")
          println(s"\tnewState: ${PrettyPrinter.print(newState)}")
          println(s"\tfor visualizer: [0, ${PrettyPrinter.print(newState)}, ${PrettyPrinter.print(data)}]")
          drawPictures(toPitures(data))
          println()
        } else {
          println(s"[0, ${PrettyPrinter.print(newState)}, ${PrettyPrinter.print(data)}]")
        }
        interact(newState, GalaxyInteract.click(verbose))
      case _ =>
        if (verbose) {
          println("send to server:")
          println(s"\tnewState: ${PrettyPrinter.print(newState)}")
          println(s"\tsending data ${PrettyPrinter.print(data)}")
        }
        val response = GalaxyInteract.sendAlienProxy(data, verbose)
        if (verbose) {
          println(s"\tresponse: ${PrettyPrinter.print(response)}")
        }
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

  def click(verbose: Boolean): Expr = {
    if(verbose) {
      println(s"input click pos:")
    }
    val x = sc.nextInt()
    val y = sc.nextInt()
    Apply(Apply(Cons, Number(x)), Number(y))
  }

  import dispatch._

  def sendAlienProxy(data: Expr, verbose: Boolean): Expr = {
    if(verbose) {
      println(s"send to alien proxy (https://icfpc2020-api.testkontur.ru/swagger/index.html)")
    }
    val response =
      Await.result(Http.default(request << Modulator.mod(data) OK as.String)(ExecutionContext.global), Duration.Inf)
    Thread.sleep(100) // 適当に待つ
    Demodulator.dem(response)
  }

  private[this] val request =
    url("https://icfpc2020-api.testkontur.ru/aliens/send")
      .addQueryParameter("apiKey", "95052afa4bf54914a26622eea251b536")
      .POST
      .setContentType("text/plain", StandardCharsets.UTF_8)

  def main(args: Array[String]): Unit =
    if (args.size >= 1 && args(0) == "-s") {
      val initState = if (args.size >= 2) Demodulator.dem(args(1)) else Nil
      val initEvent = if (args.size >= 3) Demodulator.dem(args(2)) else click(false)
      new GalaxyInteract(new GalaxyEval(Parser.parse(Source.fromURL(getClass.getResource("/galaxy.txt")).mkString)))
        .start(initState, initEvent)
    } else {
      println("usege:")
      println("\t`java -Xss1g -jar galaxy.jar [modulated state str] [modulated event str]`")
      println("\tfrom init: `java -Xss1g -jar galaxy.jar 00 11010010`")
      println("\tfrom large galaxy: `java -Xss1g -jar galaxy.jar 11011000011111011010110011010110000 110110000101100100`")
      println("\tfrom select (a): `java -Xss1g -jar galaxy.jar 110110010111110110000111010110011001100110011001101111100110010011011001001101101000110000 1101100001110111110011001001101100100`")
      println("\tmulti player: `java -Xss1g -jar galaxy.jar 1101100101111101100010110110000011001100110011001100110111110011001010111100000110111000001001110000 110110000001100000`")
      println("\tsilent mode: `java -Xss1g -jar galaxy.jar -s [modulated state str] [modulated event str]`")
      println()

      val initState = if (args.size >= 1) Demodulator.dem(args(0)) else Nil
      val initEvent = if (args.size >= 2) Demodulator.dem(args(1)) else click(true)

      new GalaxyInteract(new GalaxyEval(Parser.parse(Source.fromURL(getClass.getResource("/galaxy.txt")).mkString)), verbose = true)
        .start(initState, initEvent)
    }
}
