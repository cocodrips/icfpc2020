package yuizumi;

import static yuizumi.eval.PrettyPrinter.toPrettyString;

import java.util.*;
import yuizumi.eval.Number;
import yuizumi.eval.*;

public class Click {
    private static final Expr NIL = Nil.EXPR;

    private static Expr num(long x) { return Number.of(x); }

    private static Expr ap(Expr f, Expr x) {
        return new Apply(f, x);
    }

    private static Expr cons(Expr car, Expr cdr) {
        return (new Apply(new Apply(Cons.EXPR, car), cdr)).reduceToData();
    }

    private static Expr car(Expr pair) { return ((Pair) pair).car; }
    private static Expr cdr(Expr pair) { return ((Pair) pair).cdr; }

    private static Expr stage0(Expr expr) {
        return cons(num(0), num(0));
    }

    private static Expr stage1(Expr expr) {
        Expr outerList = car(cdr(cdr(expr)));
        Set<Long> points = new HashSet<Long>();
        for (; outerList != NIL; outerList = cdr(outerList)) {
            Expr innerList = car(outerList);
            for (; innerList != NIL; innerList = cdr(innerList)) {
                long x = car(car(innerList)).asNumber().value;
                long y = cdr(car(innerList)).asNumber().value;
                if (!points.add(x + (y * (1L << 32)))) {
                    return car(innerList);
                }
            }
        }
        return cons(num(0), num(0));
    }

    private static Expr stage2(Expr expr) {
        Expr list = car(cdr(car(cdr(expr))));
        return cons(car(list), car(cdr(list)));
    }

    public static void main(String[] args) throws Exception {
        Expr galaxy = GalaxyLoader.load().get("galaxy");

        Expr state = NIL;
        Expr point = cons(num(0), num(0));

        while (true) {
            Expr expr = ap(ap(galaxy, state), point).reduceToData();
            System.out.println(toPrettyString(expr));
            if (car(expr).asNumber().value == 1) {
                break;
            }
            Expr newState = car(cdr(expr));
            if (state.toString().equals(newState.toString())) {
                break;
            }
            state = newState;
            switch ((int) car(state).asNumber().value) {
            case 0:
                point = stage0(expr); break;
            case 1:
                point = stage1(expr); break;
            case 2:
                point = stage2(expr); break;
            }
        }
    }
}
