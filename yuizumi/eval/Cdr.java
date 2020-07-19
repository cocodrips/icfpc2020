package yuizumi.eval;

// #27
public class Cdr extends Func_1 {
    public static final Expr EXPR = new Cdr();

    private Cdr() {}

    @Override public Expr invoke(Expr x0) {
        return ((Pair) x0.reduceToFunc()).cdr;
    }

    @Override public String toString() { return "Cdr"; }
}
