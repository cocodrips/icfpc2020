package yuizumi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yuizumi.eval.*;

public class Main {
    private static final Pattern USER_FUNC = Pattern.compile(
        "^(.*?)\\s*=\\s*(.*)$");

    public static void main(String[] args) throws Exception {
        HashMap<String, Expr> env = new HashMap<>();
        env.putAll(Builtins.MAP);

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

        Expr result = (new UserFunc("$main", args, env)).reduceToData();
        System.out.println(PrettyPrinter.toPrettyString(result));
    }
}
