package yuizumi.eval;

// #24
public class I extends Func_1 {
    public static final Expr EXPR = new I();

    private I() {}

    @Override public Expr invoke(Expr x0) {
        return x0;
    }

    @Override public String toString() { return "I"; }
}
