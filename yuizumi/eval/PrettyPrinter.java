package yuizumi.eval;

public class PrettyPrinter {
    private PrettyPrinter() {}

    public static String toPrettyString(Expr expr) {
        StringBuilder sb = new StringBuilder();
        appendPrettyString(expr, sb);
        return sb.toString();
    }

    private static boolean isList(Expr expr) {
        while (expr instanceof Pair)
            expr = ((Pair) expr).cdr;
        return expr == Nil.EXPR;
    }

    private static void appendPrettyString(Expr expr, StringBuilder sb) {
        expr = expr.reduceToData();

        if (expr instanceof Number) {
            sb.append(((Number) expr).value);
            return;
        }
        if (expr == Nil.EXPR) {
            sb.append("nil");
            return;
        }

        if (isList(expr)) {
            sb.append('[');
            String sep = "";
            while (expr instanceof Pair) {
                Pair pair = (Pair) expr;
                sb.append(sep);
                appendPrettyString(pair.car, sb);
                expr = pair.cdr;
                sep = ",";
            }
            sb.append(']');
        } else {
            Pair pair = (Pair) expr;
            sb.append('(');
            appendPrettyString(pair.car, sb);
            sb.append(',');
            appendPrettyString(pair.cdr, sb);
            sb.append(')');
        }
    }
}
