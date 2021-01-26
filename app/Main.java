package app;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
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
        public Vector swapped() { return Vector.of(y, x); }
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

        // Change here to stop children!
        long shipCount = 1;
        Expr data = cons(
            200, cons(30, cons(shipCount == 1 ? 10 : 1, cons(shipCount, NIL))));
        Expr req3 = cons(3, cons(playerKey, cons(data, NIL)));
        Expr gameRes = send(URI.create(apiUrl), req3);
        System.out.println(PrettyPrinter.toPrettyString(gameRes));
        Expr staticGameInfo = idx(gameRes, 2);
        long role = idx(staticGameInfo, 1).asNumber().value;
        long mainShipId = -1;

        Random random = new Random();
        while (true) {
            long stage = idx(gameRes, 1).asNumber().value;
            if (stage == 2) { break; }
            Expr gameState = idx(gameRes, 3);
            Expr shipsAndComands = idx(gameState, 2);
            List<Expr> myShips = new ArrayList<>();
            List<Expr> otherShips = new ArrayList<>();
            while (!(shipsAndComands instanceof Nil)) {
                Expr shipAndComand = car(shipsAndComands);
                Expr ship = car(shipAndComand);
                if (idx(ship, 0).asNumber().value == role) {
                    myShips.add(ship);
                    if (mainShipId < 0) {
                        mainShipId = idx(ship, 1).asNumber().value;
                    }
                    System.out.println(
                        "executed:" + PrettyPrinter.toPrettyString(idx(shipAndComand, 1)));
                } else {
                    otherShips.add(ship);
                }
                shipsAndComands = cdr(shipsAndComands);
            }
            if (myShips.isEmpty()) {
                break;
            }
            // Generate commands.
            Expr commands = NIL;
            for (Expr ship : myShips) {
                long shipId = idx(ship, 1).asNumber().value;
                commands = createCommands(
                    commands, ship, role, shipId == mainShipId, shipCount,
                    myShips, otherShips, random);
            }
            System.out.println("commands: " + PrettyPrinter.toPrettyString(commands));
            Expr gameReq = cons(4, cons(playerKey, cons(commands, NIL)));
            gameRes = send(URI.create(apiUrl), gameReq);
            System.out.println();
            System.out.println(PrettyPrinter.toPrettyString(gameRes));
        }
    }

    private static Expr createCommands(
        Expr commands, Expr ship, long role, boolean isMain, long shipCount,
        List<Expr> myShips, List<Expr> otherShips, Random random) {
        long shipId = idx(ship, 1).asNumber().value;
        // Rotate.
        Vector pos = Vector.fromExpr(idx(ship, 2));
        System.out.println("pos: " + pos);
        Vector vel = Vector.fromExpr(idx(ship, 3));
        System.out.println("vel: " + vel);
        long gravityX = Math.abs(pos.x) >= Math.abs(pos.y) ? sign(pos.x) : 0;
        long gravityY = Math.abs(pos.y) >= Math.abs(pos.x) ? sign(pos.y) : 0;
        Vector acc = Vector.of(0, 0);
        if (gravityX != 0 && gravityY !=0) {
            acc = Vector.of(gravityY, -gravityX);
        } else if ((vel.l2Norm() < 8 && pos.l2Norm() <= 80) || pos.l2Norm() <= 35) {
            // Initial state or emergency.
            if (gravityX != 0) {
                acc = Vector.of(-gravityX, -gravityX);
            } else {
                acc = Vector.of(gravityY, -gravityY);
            }
        } else if (pos.l2Norm() < 90) {
            if (!isMain && random.nextFloat() < 0.05) {
                // Randomize non-main ships.
                acc = Vector.of(random.nextInt(2) - 1, random.nextInt(2) - 1);
            } else if (gravityX > 0) {
                acc = findAcc(pos.x, pos.y, vel.x, vel.y, gravityX, gravityY, +1);
            } else if (gravityX < 0) {
                acc = findAcc(pos.x, pos.y, vel.x, vel.y, gravityX, gravityY, -1);
            } else if (gravityY > 0) {
                acc = findAcc(pos.y, pos.x, vel.y, vel.x, gravityY, gravityX, +1).swapped();
            } else if (gravityY < 0) {
                acc = findAcc(pos.y, pos.x, vel.y, vel.x, gravityY, gravityX, -1).swapped();
            }
        }
        boolean xRisk =
            sign(pos.x) == sign(vel.x) && (Math.abs(pos.x) > 90 || Math.abs(vel.x) >= 10);
        if (xRisk) {
            System.out.println("risky x.");
            acc = Vector.of(sign(pos.x), acc.y);
        }
        boolean yRisk =
            sign(pos.y) == sign(vel.y) && (Math.abs(pos.y) > 90 || Math.abs(vel.y) >= 10);
        if (yRisk) {
            System.out.println("risky y.");
            acc = Vector.of(acc.x, sign(pos.y));
        }
        if (!acc.isZero()) {
            Expr accCommand = cons(0, cons(shipId, cons(acc.toExpr(), NIL)));
            commands = cons(accCommand, commands);
            System.out.println("accel: " + PrettyPrinter.toPrettyString(accCommand));
        }

        // Shoot.
        if (role == 0 && !otherShips.isEmpty() && random.nextFloat() < 0.3) {
            for (Expr otherShip : otherShips) {
                Vector otherPos = Vector.fromExpr(idx(otherShip, 2));
                Vector otherVel = Vector.fromExpr(idx(otherShip, 3));
                long ox = otherPos.x + otherVel.x;
                long oy = otherPos.y + otherVel.y;
                long mx = pos.x + vel.x;
                long my = pos.y + vel.y;
                if (Math.abs(ox - mx) <= 5 && Math.abs(oy - mx) <= 5
                    && myShips.size() >= otherShips.size()) {
                    Expr shootCommand = cons(1, cons(shipId, NIL));
                    commands = cons(shootCommand, commands);
                    System.out.println("bang!: " + PrettyPrinter.toPrettyString(shootCommand));
                    otherShips.remove(otherShip);
                    break;
                } else if (Math.abs(ox - mx) <= 20 && Math.abs(oy - mx) <= 20) {
                    Expr shootCommand = cons(2, cons(shipId, cons(cons(ox, oy), cons(64, NIL))));
                    commands = cons(shootCommand, commands);
                    System.out.println("shoot: " + PrettyPrinter.toPrettyString(shootCommand));
                    otherShips.remove(otherShip);
                    break;
                }
            }
        }

        // Instantiate.
        if (isMain && shipCount > 1 && random.nextFloat() < 0.1 && acc.isZero()) {
            Expr params = cons(0, cons(0, cons(0, cons(1, NIL))));
            Expr newCommand = cons(3, cons(shipId, cons(params, NIL)));
            commands = cons(newCommand, commands);
            System.out.println("new: " + PrettyPrinter.toPrettyString(newCommand));
        }
        return commands;
    }

    private static Vector findAcc(
        long posX, long posY, long velX, long velY, long gX, long gY, long dX) {
        Vector best = Vector.of(0, 0);
        if (Math.abs(posX) < 5 || Math.abs(posY) < 5) {
            System.out.println("cond can't be calced here.");
            return best;
        }
        // Fix shape
        if (Math.abs(Math.abs(posX) - Math.abs(posY)) < 10) {
            if (Math.abs(velX) > Math.abs(velY) + 2) {
                System.out.println("too narrow shape.");
                return Vector.of(sign(velX), -sign(velY));
            } else if (Math.abs(velY) > Math.abs(velX) + 2) {
                System.out.println("too narrow shape.");
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
        double originalNorm = Vector.of(velX, velY ).lInfNorm();
        for (long accX = -1; accX <= 1; accX++) {
            for (long accY = -1; accY <= 1; accY++) {
                double afterNorm = Vector.of(velX + accX, velY + accY).lInfNorm();
                if (afterNorm >= 10 && afterNorm >= originalNorm) {
                    System.out.println("too fast.");
                    continue;
                }
                long cond = dX * (velX - accX - gX) * (velY - accY - gY) + (posY + velY);
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
