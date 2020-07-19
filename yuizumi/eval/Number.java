package yuizumi.eval;

public class Number extends Expr {
    private Number(long value) {
        this.value = value;
    }

    public final long value;

    public static Number of(long value) {
        return new Number(value);
    }

    @Override public Expr reduceToFunc() { return this; }
    @Override public Expr reduceToData() { return this; }
    @Override public Number asNumber() { return this; }

    @Override public String toString() {
        return Long.toString(value);
    }
}
