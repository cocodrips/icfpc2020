package yuizumi.eval;

public abstract class Expr {
    public abstract Expr reduceToFunc();  // Does not reduce Pair elements.
    public abstract Expr reduceToData();  // Reduces Pair elements.

    public Expr apply(Expr arg) {
        throw new IllegalStateException(String.format("%s is not applicable", this));
    }

    public Number asNumber() {
        throw new IllegalStateException(String.format("%s is not a number", this));
    }
}
