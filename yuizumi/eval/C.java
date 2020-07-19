package yuizumi.eval;

// #19
public class C extends Func_3 {
    public static final Expr EXPR = new C();

    private C() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(new Apply(x0, x2), x1);
    }

    @Override public String toString() { return "C"; }
}
