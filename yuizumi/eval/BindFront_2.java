package yuizumi.eval;

public class BindFront_2 extends Func_1 {
    public BindFront_2(Func_2 fn, Expr x0) {
        this.fn = fn;
        this.x0 = x0;
    }

    public final Func_2 fn;
    public final Expr x0;

    @Override public Expr invoke(Expr x1) {
        return fn.invoke(x0, x1);
    }

    @Override public String toString() {
        return String.format("BindFront_2(%s, %s)", fn, x0);
    }
}
