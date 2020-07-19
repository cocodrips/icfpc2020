package yuizumi;

import yuizumi.eval.*;

public class Modem {
  public static void main(String[] args) throws Exception {
    Expr e = new Pair(yuizumi.eval.Number.of(1), Nil.EXPR);
    String str = Modulator.modulate(e);
    System.out.println(str);
    Expr f = Demodulator.demodulate(str);
    System.out.println(PrettyPrinter.toPrettyString(f));
  }
}
