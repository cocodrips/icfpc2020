package yuizumi.eval;

// #25
public class Cons extends Func_2 {
    public static final Expr EXPR = new Cons();

    private Cons() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return new Pair(x0, x1);
    }

    @Override public String toString() { return "Cons"; }
}
