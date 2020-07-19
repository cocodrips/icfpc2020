package yuizumi.eval;

public abstract class FuncBase extends Expr {
    @Override public Expr reduceToFunc() {
        return this;
    }

    @Override public Expr reduceToData() {
        throw new IllegalStateException(String.format("%s is not a value.", this));
    }
}
