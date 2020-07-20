package yuizumi.eval;

public class Modulator {
  private Modulator() {
  }

  public static String modulate(Expr expr) {
    StringBuilder sb = new StringBuilder();
    appendModulated(expr, sb);
    return sb.toString();
  }

  private static void appendModulated(Expr expr, StringBuilder sb) {
    expr = expr.reduceToData();
    if (expr instanceof Number) {
      appendModulatedLong(((Number) expr).value, sb);
      return;
    }
    if (expr == Nil.EXPR) {
      sb.append("00");
      return;
    }
    if (expr instanceof Pair) {
      Pair pair = (Pair) expr;
      sb.append("11");
      appendModulated(pair.car, sb);
      appendModulated(pair.cdr, sb);
    }
  }

  private static void appendModulatedLong(long i, StringBuilder sb) {
    if (i == 0) {
      sb.append("010");
      return;
    }
    if (i > 0) {
      sb.append("01");
    } else {
      sb.append("10");
      i = -i;
    }
    sb.append("1".repeat(16));
    sb.append('0');
    sb.append(String.format("%64s", Long.toBinaryString(i)).replace(' ', '0'));
  }
}
