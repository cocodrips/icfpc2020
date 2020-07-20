package app;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.Random;
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
    private static Expr cons(long car, long cdr) {
        return new Pair(Number.of(car), Number.of(cdr));
    }

    private static Expr car(Expr expr) { return ((Pair) expr).car; }
    private static Expr cdr(Expr expr) { return ((Pair) expr).cdr; }
    private static Expr idx(Expr expr, int i) {
        if (i == 0) {
            if (expr instanceof Pair) { return car(expr); }
            else { return null; }
        } else {
            if (expr instanceof Pair) { return idx(cdr(expr), i - 1); }
            else { return null; }
        }
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
            if (apiUrl.startsWith("https://icfpc2020-api.testkontur.ru")) {
                System.out.println("But on test server.");
                apiUrl += "?apiKey=" + API_KEY;
            }
        } else {
            System.out.println("Running locally.");
            apiUrl += "?apiKey=" + API_KEY;
            galaxy = GalaxyLoader.load().get("galaxy");
        }

        Expr req2 = cons(2, cons(playerKey, cons(NIL, NIL)));
        Expr res2 = send(URI.create(apiUrl), req2);

        System.out.println(PrettyPrinter.toPrettyString(res2));

        Expr data = cons(150, cons(0, cons(8, cons(100, NIL))));
        Expr req3 = cons(3, cons(playerKey, cons(data, NIL)));
        Expr gameRes = send(URI.create(apiUrl), req3);
        Expr staticGameInfo = idx(gameRes, 2);
        long role = idx(staticGameInfo, 1).asNumber().value;

        System.out.println(PrettyPrinter.toPrettyString(gameRes));
        Random random = new Random();
        long grabX = 0;
        long grabY = 0;
        while (true) {
            long stage = idx(gameRes, 1).asNumber().value;
            if (stage == 2) { break; }
            Expr gameState = idx(gameRes, 3);
            long turn = idx(gameState, 0).asNumber().value;
            Expr shipsAndComands = idx(gameState, 2);
            Expr myShip = null;
            Expr otherShip = null;
            while (!(shipsAndComands instanceof Nil)) {
                Expr shipAndComand = car(shipsAndComands);
                Expr ship = car(shipAndComand);
                if (idx(ship, 0).asNumber().value == role) {
                    myShip = ship;
                } else {
                    otherShip = ship;
                }
                shipsAndComands = cdr(shipsAndComands);
            }
            if (myShip == null) {
                break;
            }
            // Rotate.
            long shipId = idx(myShip, 1).asNumber().value;
            Expr position = idx(myShip, 2); // vector
            Expr velocity = idx(myShip, 3); // vector
            long posX = car(position).asNumber().value;
            long posY = cdr(position).asNumber().value;
            long velX = car(velocity).asNumber().value;
            long velY = cdr(velocity).asNumber().value;
            if (turn == 0) {
              grabX = velX;
              grabY = velY;
            }
            long accX = grabX;
            long accY = grabY;
            Expr acc = cons(accX, accY);
            Expr command = cons(0, cons(shipId, cons(acc, NIL)));

            System.out.println("command: " + PrettyPrinter.toPrettyString(command));
            Expr commands = cons(command, NIL);
            Expr gameReq = cons(4, cons(playerKey, cons(commands, NIL)));
            gameRes = send(URI.create(apiUrl), gameReq);
            System.out.println(PrettyPrinter.toPrettyString(gameRes));
        }
    }
    static long sign(long num) {
        return num < 0 ? -1 : (num > 0 ? 1 : 0);
    }
}
