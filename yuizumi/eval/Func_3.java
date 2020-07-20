package yuizumi.eval;

public abstract class Func_3 extends FuncBase {
    @Override public Expr apply(Expr x) {
        return new BindFront_3(this, x);
    }

    public abstract Expr invoke(Expr x0, Expr x1, Expr x2);
}
