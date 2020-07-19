package yuizumi.eval;

// #21
public class T extends Func_2 {
    public static final Expr EXPR = new T();

    private T() {}

    @Override public Expr invoke(Expr x0, Expr x1) { return x0; }

    @Override public String toString() { return "T"; }
}
