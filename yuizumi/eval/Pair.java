package yuizumi.eval;

public class Pair extends ReducibleExpr {
    public Pair(Expr car, Expr cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public final Expr car;
    public final Expr cdr;

    @Override protected Expr doReduceToFunc() {
        return this;
    }

    @Override protected Expr doReduceToData() {
        return new Pair(car.reduceToData(), cdr.reduceToData());
    }

    @Override public Expr apply(Expr x) {
        return new Apply(new Apply(x, car), cdr);
    }

    @Override public String toString() {
        return String.format("Pair(%s, %s)", car, cdr);
    }
}
