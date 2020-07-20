package yuizumi;

import java.util.Map;

import yuizumi.eval.*;

public class Eval {
    public static void main(String[] args) throws Exception {
        Map<String, Expr> env = GalaxyLoader.load();
        Expr result = (new UserFunc("$main", args, env)).reduceToData();
        System.out.println(PrettyPrinter.toPrettyString(result));
    }
}
