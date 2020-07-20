package yuizumi.eval;

// #16
public class Neg extends Func_1 {
    public static final Expr EXPR = new Neg();

    private Neg() {}

    @Override public Expr invoke(Expr x0) {
        return Number.of(-x0.asNumber().value);
    }

    @Override public String toString() { return "Neg"; }
}
