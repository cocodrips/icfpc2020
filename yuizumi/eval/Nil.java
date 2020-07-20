package yuizumi.eval;

// #28
public class Nil extends Func_1 {
    public static final Expr EXPR = new Nil();

    private Nil() {}

    @Override public Expr invoke(Expr x0) { return T.EXPR; }

    @Override public Expr reduceToData() { return this; }

    @Override public String toString() { return "Nil"; }
}
