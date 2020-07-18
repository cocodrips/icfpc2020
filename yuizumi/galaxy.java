import java.io.*;
import java.util.*;
import java.util.regex.*;

abstract class Expr {
    public abstract Expr halfReduce();
    public abstract Expr fullReduce();
    public abstract Number asNumber();

    public Expr apply(Expr arg) {
        throw new IllegalStateException(String.format("%s is not applicable", this));
    }
}

abstract class ReducibleExpr extends Expr {
    private Expr half = null;
    private Expr full = null;

    @Override public Expr halfReduce() {
        return (half != null) ? half : (half = doHalfReduce());
    }

    @Override public Expr fullReduce() {
        return (full != null) ? full : (full = doFullReduce());
    }

    @Override public Number asNumber() {
        return fullReduce().asNumber();
    }

    protected abstract Expr doHalfReduce();
    protected abstract Expr doFullReduce();
}

abstract class GroundExpr extends Expr {
    @Override public Expr halfReduce() { return this; }
    @Override public Expr fullReduce() { return this; }

    @Override public Number asNumber() {
        throw new IllegalStateException(String.format("%s is not a number", this));
    }
}

class Number extends GroundExpr {
    private Number(long value) {
        this.value = value;
    }

    public final long value;

    public static Number of(long value) {
        return new Number(value);
    }

    @Override public Number asNumber() {
        return this;
    }

    @Override public String toString() {
        return Long.toString(value);
    }
}

class Pair extends ReducibleExpr {
    public Pair(Expr car, Expr cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public final Expr car;
    public final Expr cdr;

    @Override protected Expr doHalfReduce() {
        return this;
    }

    @Override protected Expr doFullReduce() {
        return new Pair(car.fullReduce(), cdr.fullReduce());
    }

    @Override public Expr apply(Expr x) {
        return new Apply(new Apply(x, car), cdr);
    }

    @Override public String toString() {
        return String.format("(%s, %s)", car, cdr);
    }
}

abstract class Func_1 extends GroundExpr {
    @Override public Expr apply(Expr x) {
        return invoke(x);
    }

    public abstract Expr invoke(Expr x0);
}

abstract class Func_2 extends GroundExpr {
    @Override public Expr apply(Expr x) {
        return new BindFront_2(this, x);
    }

    public abstract Expr invoke(Expr x0, Expr x1);
}

abstract class Func_3 extends GroundExpr {
    @Override public Expr apply(Expr x) {
        return new BindFront_3(this, x);
    }

    public abstract Expr invoke(Expr x0, Expr x1, Expr x2);
}

class BindFront_2 extends Func_1 {
    public BindFront_2(Func_2 fn, Expr x0) {
        this.fn = fn;
        this.x0 = x0;
    }

    public final Func_2 fn;
    public final Expr x0;

    @Override public Expr invoke(Expr x1) {
        return fn.invoke(x0, x1);
    }

    @Override public String toString() {
        return String.format("ap %s %s", fn, x0);
    }
}

class BindFront_3 extends Func_2 {
    public BindFront_3(Func_3 fn, Expr x0) {
        this.fn = fn;
        this.x0 = x0;
    }

    public final Func_3 fn;
    public final Expr x0;

    @Override public Expr invoke(Expr x1, Expr x2) {
        return fn.invoke(x0, x1, x2);
    }

    @Override public String toString() {
        return String.format("ap %s %s", fn, x0);
    }
}

// #7
class Add extends Func_2 {
    public static final Expr EXPR = new Add();

    private Add() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value + x1.asNumber().value);
    }

    @Override public String toString() { return "add"; }
}

// #9
class Mul extends Func_2 {
    public static final Expr EXPR = new Mul();

    private Mul() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value * x1.asNumber().value);
    }

    @Override public String toString() { return "mul"; }
}

// #10
class Div extends Func_2 {
    public static final Expr EXPR = new Div();

    private Div() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return Number.of(x0.asNumber().value / x1.asNumber().value);
    }

    @Override public String toString() { return "div"; }
}

// #11
class Eq extends Func_2 {
    public static final Expr EXPR = new Eq();

    private Eq() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return (x0.asNumber().value == x1.asNumber().value) ? True.EXPR : False.EXPR;
    }

    @Override public String toString() { return "eq"; }
}

// #11
class Lt extends Func_2 {
    public static final Expr EXPR = new Lt();

    private Lt() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return (x0.asNumber().value <  x1.asNumber().value) ? True.EXPR : False.EXPR;
    }

    @Override public String toString() { return "lt"; }
}

// #16
class Neg extends Func_1 {
    public static final Expr EXPR = new Neg();

    private Neg() {}

    @Override public Expr invoke(Expr x0) {
        return Number.of(-x0.asNumber().value);
    }

    @Override public String toString() { return "neg"; }
}

// #17
class Apply extends ReducibleExpr {
    public Apply(Expr f, Expr x) {
        this.f = f;
        this.x = x;
    }

    public final Expr f;
    public final Expr x;

    @Override public Expr apply(Expr x) {
        return f.halfReduce().apply(x);
    }

    @Override protected Expr doHalfReduce() {
        return apply(x).halfReduce();
    }

    @Override protected Expr doFullReduce() {
        return apply(x).fullReduce();
    }

    @Override public String toString() {
        return String.format("ap %s %s", f, x);
    }
}

// #18
class SCombinator extends Func_3 {
    public static final Expr EXPR = new SCombinator();

    private SCombinator() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(new Apply(x0, x2), new Apply(x1, x2));
    }

    @Override public String toString() { return "s"; }
}

// #19
class CCombinator extends Func_3 {
    public static final Expr EXPR = new CCombinator();

    private CCombinator() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(new Apply(x0, x2), x1);
    }

    @Override public String toString() { return "c"; }
}

// #20
class BCombinator extends Func_3 {
    public static final Expr EXPR = new BCombinator();

    private BCombinator() {}

    @Override public Expr invoke(Expr x0, Expr x1, Expr x2) {
        return new Apply(x0, new Apply(x1, x2));
    }

    @Override public String toString() { return "b"; }
}

// #21
class True extends Func_2 {
    public static final Expr EXPR = new True();

    private True() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return x0;
    }

    @Override public String toString() { return "t"; }
}

// #22
class False extends Func_2 {
    public static final Expr EXPR = new False();

    private False() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return x1;
    }

    @Override public String toString() { return "f"; }
}

// #24
class ICombinator extends Func_1 {
    public static final Expr EXPR = new ICombinator();

    private ICombinator() {}

    @Override public Expr invoke(Expr x0) {
        return x0;
    }

    @Override public String toString() { return "i"; }
}

// #25
class Cons extends Func_2 {
    public static final Expr EXPR = new Cons();

    private Cons() {}

    @Override public Expr invoke(Expr x0, Expr x1) {
        return new Pair(x0, x1);
    }

    @Override public String toString() { return "cons"; }
}

// #26
class Car extends Func_1 {
    public static final Expr EXPR = new Car();

    private Car() {}

    @Override public Expr invoke(Expr x0) {
        return ((Pair) x0.halfReduce()).car;
    }

    @Override public String toString() { return "car"; }
}

// #27
class Cdr extends Func_1 {
    public static final Expr EXPR = new Cdr();

    private Cdr() {}

    @Override public Expr invoke(Expr x0) {
        return ((Pair) x0.halfReduce()).cdr;
    }

    @Override public String toString() { return "cdr"; }
}

// #28
class Nil extends Func_1 {
    public static final Expr EXPR = new Nil();

    private Nil() {}

    @Override public Expr invoke(Expr x0) {
        return True.EXPR;
    }

    @Override public String toString() { return "nil"; }
}

// #29
class IsNil extends Func_1 {
    public static final Expr EXPR = new IsNil();

    private IsNil() {}

    @Override public Expr invoke(Expr x0) {
        return (x0.fullReduce() == Nil.EXPR) ? True.EXPR : False.EXPR;
    }
}

class UserFunc extends ReducibleExpr {
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
        @Override public String toString() { return "AP"; }

        @Override public Expr halfReduce() { throw new IllegalStateException(); }
        @Override public Expr fullReduce() { throw new IllegalStateException(); }
        @Override public Expr apply(Expr arg) { throw new IllegalStateException(); }
        @Override public Number asNumber() { throw new IllegalStateException(); }
    };

    @Override protected Expr doHalfReduce() {
        return compile().halfReduce();
    }

    @Override protected Expr doFullReduce() {
        return compile().fullReduce();
    }

    private Expr tokenize(String token) {
        if (token.equals("ap")) {
            return AP;
        }
        if (env.containsKey(token)) {
            return env.get(token);
        }
        if (NUMBER.matcher(token).matches()) {
            return Number.of(Long.parseLong(token));
        }

        throw new IllegalArgumentException(token);
    }

    private Expr compile() {
        if (compiled != null) { return compiled; }

        ArrayList<Expr> stack = new ArrayList<>();

        for (String token : rhs) {
            stack.add(tokenize(token));

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
                    throw new IllegalArgumentException("parse error: " + lhs);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("parse error: " + lhs);
        }

        return (compiled = stack.get(0));
    }

    @Override public String toString() { return lhs; }
}

class Main {
    private static final Pattern USER_FUNC = Pattern.compile(
        "^(.*?)\\s*=\\s*(.*)$");

    public static void main(String[] args) throws Exception {
        Map<String, Expr> env = new HashMap<>();

        env.put("add", Add.EXPR);
        env.put("mul", Mul.EXPR);
        env.put("div", Div.EXPR);
        env.put("eq", Eq.EXPR);
        env.put("lt", Lt.EXPR);
        env.put("neg", Neg.EXPR);
        env.put("s", SCombinator.EXPR);
        env.put("c", CCombinator.EXPR);
        env.put("b", BCombinator.EXPR);
        env.put("i", ICombinator.EXPR);
        env.put("t", True.EXPR);
        env.put("f", False.EXPR);
        env.put("cons", Cons.EXPR);
        env.put("car", Car.EXPR);
        env.put("cdr", Cdr.EXPR);
        env.put("nil", Nil.EXPR);
        env.put("isnil", IsNil.EXPR);

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = USER_FUNC.matcher(line);
            if (matcher.matches()) {
                String lhs = matcher.group(1);
                String rhs = matcher.group(2);
                env.put(lhs, new UserFunc(lhs, rhs.split("\\s+"), env));
            } else {
                throw new UnsupportedOperationException();
            }
        }

        Expr result = (new UserFunc("<stdin>", args, env)).fullReduce();
        System.out.println(result);
    }
}
