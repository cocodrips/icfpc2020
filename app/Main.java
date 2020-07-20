package app;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.Random;
import java.util.Vector;

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

    public static class Vector {
        private Vector(long x, long y) {
            this.x = x;
            this.y = y;
        }
        public static Vector of(long x, long y) { return new Vector(x, y); }
        public static Vector fromExpr(Expr expr) {
            return Vector.of(car(expr).asNumber().value, cdr(expr).asNumber().value);
        }
        public Vector orthogonal() { return Vector.of(y, -x); }
        public Expr toExpr() { return cons(x, y); }
        public boolean isZero() { return x == 0 && y == 0; }
        public long l1Norm() { return Math.abs(x) + Math.abs(y); }
        public double l2Norm() { return Math.sqrt(x * x + y * y); }
        public long lInfNorm() { return Math.max(Math.abs(x), Math.abs(y)); }
        @Override public String toString() { return "(" + x + "," + y + ")"; };
        public final long x;
        public final long y;
    }

    public static void main(String[] args) throws Exception {
        String apiUrl = args[0] + "/aliens/send";
        long playerKey = Long.parseLong(args[1]);
        if (DOCKER_GALAXY_TXT.exists()) {
            System.out.println("Running on Docker.");
            String path = DOCKER_GALAXY_TXT.getPath();
            if (apiUrl.startsWith("https://icfpc2020-api.testkontur.ru")) {
                System.out.println("But on test server.");
                apiUrl += "?apiKey=" + API_KEY;
            }
        } else {
            System.out.println("Running locally.");
            apiUrl += "?apiKey=" + API_KEY;
        }

        Expr req2 = cons(2, cons(playerKey, cons(NIL, NIL)));
        Expr res2 = send(URI.create(apiUrl), req2);

        System.out.println(PrettyPrinter.toPrettyString(res2));

        Expr data = cons(200, cons(30, cons(10, cons(1, NIL))));
        Expr req3 = cons(3, cons(playerKey, cons(data, NIL)));
        Expr gameRes = send(URI.create(apiUrl), req3);
        System.out.println(PrettyPrinter.toPrettyString(gameRes));
        Expr staticGameInfo = idx(gameRes, 2);
        long role = idx(staticGameInfo, 1).asNumber().value;

        Random random = new Random();
        while (true) {
            long stage = idx(gameRes, 1).asNumber().value;
            if (stage == 2) { break; }
            Expr gameState = idx(gameRes, 3);
            Expr shipsAndComands = idx(gameState, 2);
            Expr myShip = null;
            Expr otherShip = null;
            while (!(shipsAndComands instanceof Nil)) {
                Expr shipAndComand = car(shipsAndComands);
                Expr ship = car(shipAndComand);
                if (idx(ship, 0).asNumber().value == role) {
                    myShip = ship;
                    System.out.println(
                        "executed:" + PrettyPrinter.toPrettyString(idx(shipAndComand, 1)));
                } else {
                    otherShip = ship;
                }
                shipsAndComands = cdr(shipsAndComands);
            }
            if (myShip == null) {
                break;
            }

            // Generate commands.
            Expr commands = NIL;

            // Rotate.
            long shipId = idx(myShip, 1).asNumber().value;
            Vector pos = Vector.fromExpr(idx(myShip, 2));
            System.out.println("pos: " + pos);
            Vector vel = Vector.fromExpr(idx(myShip, 3));
            System.out.println("vel: " + vel);
            long gravityX = Math.abs(pos.x) >= Math.abs(pos.y) ? sign(pos.x) : 0;
            long gravityY = Math.abs(pos.y) >= Math.abs(pos.x) ? sign(pos.y) : 0;
            Vector acc = Vector.of(0, 0);
            if (gravityX != 0 && gravityY !=0) {
                acc = Vector.of(-gravityX, -gravityY);
            } else if ((vel.l2Norm() < 8 && pos.l2Norm() <= 80) || pos.l2Norm() <= 35) {
                // Initial state or emergency.
                if (gravityX != 0) {
                    acc = Vector.of(-gravityX, -gravityX);
                } else {
                    acc = Vector.of(gravityY, -gravityY);
                }
            } else {
                if (gravityX > 0) {
                    acc = findAcc(pos.x, pos.y, vel.x, vel.y, +1);
                } else if (gravityX < 0) {
                    acc = findAcc(pos.x, pos.y, vel.x, vel.y, -1);
                } else if (gravityY > 0) {
                    acc = findAcc(pos.y, pos.x, vel.y, vel.x, +1).orthogonal();
                } else if (gravityY < 0) {
                    acc = findAcc(pos.y, pos.x, vel.y, vel.x, -1).orthogonal();
                }
            }
            if (!acc.isZero()) {
                Expr accCommand = cons(0, cons(shipId, cons(acc.toExpr(), NIL)));
                commands = cons(accCommand, commands);
                System.out.println("accel: " + PrettyPrinter.toPrettyString(accCommand));
            }

            // Shoot.
            if (role == 0 && otherShip != null && random.nextFloat() < 0.3) {
                Vector otherPos = Vector.fromExpr(idx(otherShip, 2));
                Vector otherVel = Vector.fromExpr(idx(otherShip, 3));
                Expr target = cons(otherPos.x + otherVel.x, otherPos.y + otherVel.y);
                Expr shootCommand = cons(2, cons(shipId, cons(target, cons(64, NIL))));
                commands = cons(shootCommand, commands);
                System.out.println("shoot: " + PrettyPrinter.toPrettyString(shootCommand));
            }

            System.out.println("commands: " + PrettyPrinter.toPrettyString(commands));
            Expr gameReq = cons(4, cons(playerKey, cons(commands, NIL)));
            gameRes = send(URI.create(apiUrl), gameReq);
            System.out.println(PrettyPrinter.toPrettyString(gameRes));
        }
    }

    private static Vector findAcc(long posX, long posY, long velX, long velY, long dX) {
        Vector best = Vector.of(0, 0);
        // Fix shape
        if (Math.abs(Math.abs(posX) - Math.abs(posY)) < 10) {
            if (Math.abs(velX) > Math.abs(velY) + 2) {
                System.out.println("too horizontal shape.");
                return Vector.of(sign(velX), -sign(velY));
            } else if (Math.abs(velY) > Math.abs(velX) + 2) {
                System.out.println("too vertical shape.");
                return Vector.of(-sign(velX), sign(velY));
            }
        }
        long bestCond = dX * velX * velY + (posY + velY);
        System.out.println("default cond: " + bestCond);
        // Save energy!
        long potentialChangePerTurn = Math.max(Math.abs(velX), Math.abs(velY));
        if (Math.abs(bestCond) < potentialChangePerTurn) {
            System.out.println("energy saved as cond is under: " + potentialChangePerTurn);
            return best;
        }
        for (long accX = -2; accX <= -2; accX++) {
            for (long accY = -2; accY <= -2; accY++) {
                if (Math.abs(accX) + Math.abs(accY) > 2) {
                    continue;
                }
                long cond = dX * (velX - accX) * (velY - accY) + (posY + velY);
                if (Math.abs(cond) < Math.abs(bestCond)) {
                    bestCond = cond;
                    best = Vector.of(accX, accY);
                }
            }
        }
        System.out.println("next cond: " + bestCond);
        return best;
    }
    
    static long sign(long num) {
        return num < 0 ? -1 : (num > 0 ? 1 : 0);
    }
}
