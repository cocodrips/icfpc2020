package yuizumi.eval;

// #10
public class Div extends Func_2 {
    public static final Expr EXPR = new Div();

    private Div() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value / x1.asNumber().value);
    }

    @Override public String toString() { return "Div"; }
}
