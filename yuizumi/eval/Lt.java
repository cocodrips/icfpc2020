package yuizumi.eval;

// #12
public class Lt extends Func_2 {
    public static final Expr EXPR = new Lt();

    private Lt() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return (x0.asNumber().value < x1.asNumber().value) ? T.EXPR : F.EXPR;
    }

    @Override public String toString() { return "Lt"; }
}
