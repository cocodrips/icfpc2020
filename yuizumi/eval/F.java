package yuizumi.eval;

// #22
public class F extends Func_2 {
    public static final Expr EXPR = new F();

    private F() {}

    @Override public Expr invoke(Expr x0, Expr x1) { return x1; }

    @Override public String toString() { return "F"; }
}
