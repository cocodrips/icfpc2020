package yuizumi.eval;

public abstract class ReducibleExpr extends Expr {
    private Expr func = null;
    private Expr data = null;

    protected abstract Expr doReduceToFunc();
    protected abstract Expr doReduceToData();

    @Override public Number asNumber() {
        return reduceToData().asNumber();
    }

    @Override public Expr reduceToFunc() {
        return (func != null) ? func : (func = doReduceToFunc());
    }

    @Override public Expr reduceToData() {
        return (data != null) ? data : (data = doReduceToData());
    }
}
