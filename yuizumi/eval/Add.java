package yuizumi.eval;

// #7
public class Add extends Func_2 {
    public static final Expr EXPR = new Add();

    private Add() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value + x1.asNumber().value);
    }

    @Override public String toString() { return "Add"; }
}
