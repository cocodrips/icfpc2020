package app;

import java.io.*;
import java.net.*;
import java.net.http.*;
import yuizumi.GalaxyLoader;
import yuizumi.eval.*;
import yuizumi.eval.Number;

class Main {
    private static final String API_KEY = "95052afa4bf54914a26622eea251b536";

    private static final File DOCKER_GALAXY_TXT =
        new File("/solution/app/build/galaxy.txt");

    private static Expr send(URI uri, Expr reqExpr) throws Exception {
        String reqBody = Modulator.modulate(reqExpr);

        HttpRequest request = HttpRequest.newBuilder().uri(uri)
            .version(HttpClient.Version.HTTP_1_1)
            .POST(HttpRequest.BodyPublishers.ofString(reqBody))
            .build();

        HttpResponse response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        var status = response.statusCode();

        if (status != HttpURLConnection.HTTP_OK) {
            System.out.println("Unexpected server response:");
            System.out.println("HTTP code: " + status);
            System.out.println("Response body: " + response.body());
            throw new RuntimeException();
        }

        return Demodulator.demodulate(response.body().toString());
    }

    private static Expr cons(Expr car, Expr cdr) {
        return new Pair(car, cdr);
    }
    private static Expr cons(long car, Expr cdr) {
        return new Pair(Number.of(car), cdr);
    }

    private static Expr NIL = Nil.EXPR;

    public static void main(String[] args) throws Exception {
        String apiUrl = args[0] + "/aliens/send";
        long playerKey = Long.parseLong(args[1]);

        Expr galaxy;

        if (DOCKER_GALAXY_TXT.exists()) {
            System.out.println("Running on Docker.");
            String path = DOCKER_GALAXY_TXT.getPath();
            galaxy = GalaxyLoader.load(path).get("galaxy");
        } else {
            System.out.println("Running locally.");
            apiUrl += "?apiKey=" + API_KEY;
            galaxy = GalaxyLoader.load().get("galaxy");
        }

        Expr req0 = cons(2, cons(playerKey, cons(NIL, NIL)));
        Expr res0 = send(URI.create(apiUrl), req0);

        System.out.println(PrettyPrinter.toPrettyString(res0));

        Expr req1 = cons(3, cons(cons(442, cons(1, cons(0, cons(1, NIL)))), NIL));
        Expr res1 = send(URI.create(apiUrl), req1);

        System.out.println(PrettyPrinter.toPrettyString(res1));
    }
}
