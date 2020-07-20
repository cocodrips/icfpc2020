package yuizumi.eval;

// #17
public class Apply extends ReducibleExpr {
    public Apply(Expr f, Expr x) {
        this.f = f;
        this.x = x;
    }

    public final Expr f;
    public final Expr x;

    @Override public Expr apply(Expr x) {
        return f.reduceToFunc().apply(x);
    }

    @Override protected Expr doReduceToFunc() {
        return apply(x).reduceToFunc();
    }

    @Override protected Expr doReduceToData() {
        return apply(x).reduceToData();
    }

    @Override public String toString() {
        return String.format("Apply(%s, %s)", f, x);
    }
}
