package yuizumi.eval;

// #18
public class S extends Func_3 {
    public static final Expr EXPR = new S();

    private S() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(new Apply(x0, x2), new Apply(x1, x2));
    }

    @Override public String toString() { return "S"; }
}
