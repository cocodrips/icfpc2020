package yuizumi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yuizumi.eval.*;

public class GalaxyLoader {
    private static final String GALAXY_TXT = "official/galaxy.txt";

    private static final Pattern USER_FUNC = Pattern.compile(
        "^(?<lhs>.*?)\\s*=\\s*(?<rhs>.*)$");

    public static Map<String, Expr> load() throws IOException {
        return load(GALAXY_TXT);
    }

    public static Map<String, Expr> load(String path) throws IOException {
        Map<String, Expr> bindings = new HashMap<>();
        bindings.putAll(Builtins.MAP);

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = USER_FUNC.matcher(line);
            if (matcher.matches()) {
                String lhs = matcher.group("lhs");
                String rhs = matcher.group("rhs");
                bindings.put(lhs, new UserFunc(lhs, rhs.split("\\s+"), bindings));
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return Collections.unmodifiableMap(bindings);
    }
}
