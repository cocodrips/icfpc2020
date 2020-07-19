package yuizumi.eval;

// #26
public class Car extends Func_1 {
    public static final Expr EXPR = new Car();

    private Car() {}

    @Override public Expr invoke(Expr x0) {
        return ((Pair) x0.reduceToFunc()).car;
    }

    @Override public String toString() { return "Car"; }
}
