package yuizumi.eval;

public abstract class Func_2 extends FuncBase {
    @Override public Expr apply(Expr x) {
        return new BindFront_2(this, x);
    }

    public abstract Expr invoke(Expr x0, Expr x1);
}
