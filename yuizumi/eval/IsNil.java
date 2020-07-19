package yuizumi.eval;

// #29
public class IsNil extends Func_1 {
    public static final Expr EXPR = new IsNil();

    private IsNil() {}

    @Override public Expr invoke(Expr x0) {
        return (x0.reduceToData() == Nil.EXPR) ? T.EXPR : F.EXPR;
    }

    @Override public String toString() { return "IsNil"; }
}
