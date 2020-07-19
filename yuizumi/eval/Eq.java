package yuizumi.eval;

// #11
public class Eq extends Func_2 {
    public static final Expr EXPR = new Eq();

    private Eq() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return (x0.asNumber().value == x1.asNumber().value) ? T.EXPR : F.EXPR;
    }

    @Override public String toString() { return "Eq"; }
}
