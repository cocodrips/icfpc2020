package yuizumi.eval;

public abstract class Func_1 extends FuncBase {
    @Override public Expr apply(Expr x) {
        return invoke(x);
    }

    public abstract Expr invoke(Expr x0);
}
