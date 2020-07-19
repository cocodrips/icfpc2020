package yuizumi.eval;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Builtins {
    public static final Map<String, Expr> MAP = buildMap();

    private Builtins() {}

    private static final Map<String, Expr> buildMap() {
        Map<String, Expr> map = new HashMap<>();
        map.put("add", Add.EXPR);
        map.put("mul", Mul.EXPR);
        map.put("div", Div.EXPR);
        map.put("eq", Eq.EXPR);
        map.put("lt", Lt.EXPR);
        map.put("neg", Neg.EXPR);
        map.put("s", S.EXPR);
        map.put("c", C.EXPR);
        map.put("b", B.EXPR);
        map.put("i", I.EXPR);
        map.put("t", T.EXPR);
        map.put("f", F.EXPR);
        map.put("cons", Cons.EXPR);
        map.put("car", Car.EXPR);
        map.put("cdr", Cdr.EXPR);
        map.put("nil", Nil.EXPR);
        map.put("isnil", IsNil.EXPR);
        return Collections.unmodifiableMap(map);
    }
}
