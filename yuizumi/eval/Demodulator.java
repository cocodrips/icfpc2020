package yuizumi.eval;

public class Demodulator {
  private Demodulator() {
  }

  static class StatefulDemod {
    String str;
    int pos;

    StatefulDemod(String str) {
      this.str = str;
      this.pos = 0;
    }

    Expr demodulate() {
      String header = str.substring(pos, pos + 2);
      pos += 2;
      if (header.length() != 2) {
        throw new IllegalArgumentException("ilformed 01 string: incomplete header");
      }
      if (header.equals("00")) {
        return Nil.EXPR;
      }
      if (header.equals("11")) {
        Expr e1 = demodulate();
        Expr e2 = demodulate();
        return new Pair(e1, e2);
      }
      int sign = header.equals("01") ? +1 : -1;
      int size = -1;
      for (int i = pos; i < str.length(); ++i) {
        if (str.charAt(i) == '0') {
          size = i - pos;
          break;
        }
      }
      if (size == -1) {
        throw new IllegalArgumentException("ilformed 01 string: ilformed integer");
      }
      pos += size + 1;
      if (size == 0) {
        return Number.of(0);
      }
      String repr = str.substring(pos, pos + 4 * size);
      pos += 4 * size;
      long value = sign * Long.valueOf(repr);
      return Number.of(value);
    }

  }

  public static Expr demodulate(String str) {
    var sd = new StatefulDemod(str);
    Expr e = sd.demodulate();
    if (sd.pos != sd.str.length()) {
      throw new IllegalArgumentException("ilformed 01 string: input has unnessesary suffix");
    }
    return e;
  }
}
