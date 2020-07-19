package yuizumi.eval;

public class BindFront_3 extends Func_2 {
    public BindFront_3(Func_3 fn, Expr x0) {
        this.fn = fn;
        this.x0 = x0;
    }

    public final Func_3 fn;
    public final Expr x0;

    @Override public Expr invoke(Expr x1, Expr x2) {
        return fn.invoke(x0, x1, x2);
    }

    @Override public String toString() {
        return String.format("BindFront_3(%s, %s)", fn, x0);
    }
}
