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

public class GalaxyReader {
    private static String GALAXY_TXT = "official/galaxy.txt";

    private static final Pattern USER_FUNC = Pattern.compile("^(.*?)\\s*=\\s*(.*)$");

    public static void setGalaxyPath(String path) {
        GALAXY_TXT = path;
    }

    public static Map<String, Expr> env() throws IOException {
        Map<String, Expr> env = new HashMap<>();
        env.putAll(Builtins.MAP);

        BufferedReader reader = new BufferedReader(
            new FileReader(GALAXY_TXT));
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

        return Collections.unmodifiableMap(env);
    }

    public static Expr galaxy() throws IOException { return env().get("galaxy"); }
}
