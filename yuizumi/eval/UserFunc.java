package yuizumi.eval;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class UserFunc extends ReducibleExpr {
    public UserFunc(String lhs, String[] rhs, Map<String, Expr> env) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.env = env;
    }

    public final String lhs;
    public final String[] rhs;
    public final Map<String, Expr> env;

    private Expr compiled = null;

    private static final Pattern NUMBER = Pattern.compile("^-?[0-9]+$");

    private static final Expr AP = new Expr() {
        @Override public Expr reduceToFunc() { throw new IllegalStateException(); }
        @Override public Expr reduceToData() { throw new IllegalStateException(); }
        @Override public String toString() { return "AP"; }
    };

    @Override protected Expr doReduceToFunc() {
        return compile().reduceToFunc();
    }

    @Override protected Expr doReduceToData() {
        return compile().reduceToData();
    }

    private Expr parseToken(String token) {
        if (token.equals("ap")) return AP;

        if (NUMBER.matcher(token).matches()) {
            return Number.of(Long.parseLong(token));
        }

        return env.get(token);
    }

    private Expr compile() {
        if (compiled != null) { return compiled; }

        ArrayList<Expr> stack = new ArrayList<>();

        for (String tok : rhs) {
            Expr expr = parseToken(tok);
            if (expr == null) {
                throw new IllegalArgumentException(String.format("token error: %s", tok));
            }
            stack.add(expr);

            while (stack.size() >= 3) {
                if (stack.get(stack.size() - 1) == AP) {
                    break;
                }
                if (stack.get(stack.size() - 2) == AP) {
                    break;
                }
                if (stack.get(stack.size() - 3) == AP) {
                    Expr x = stack.remove(stack.size() - 1);
                    Expr f = stack.remove(stack.size() - 1);
                    /* AP */ stack.remove(stack.size() - 1);
                    stack.add(new Apply(f, x));
                } else {
                    throw new IllegalArgumentException(String.format("parse error: %s", lhs));
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException(String.format("parse error: %s", lhs));
        }

        return (compiled = stack.get(0));
    }

    @Override public String toString() { return String.format("UserFunc(%s)", lhs); }
}
