package yuizumi.eval;

// #9
public class Mul extends Func_2 {
    public static final Expr EXPR = new Mul();

    private Mul() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value * x1.asNumber().value);
    }

    @Override public String toString() { return "Mul"; }
}
