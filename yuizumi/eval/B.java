package yuizumi.eval;

// #20
public class B extends Func_3 {
    public static final Expr EXPR = new B();

    private B() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(x0, new Apply(x1, x2));
    }

    @Override public String toString() { return "B"; }
}
