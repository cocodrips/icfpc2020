import java.net.*;
import java.net.http.*;
import yuizumi.eval.*;
import yuizumi.GalaxyReader;

class Main {
    public static Expr sendQuery(String serverUrl, Expr requestExpr) {
        try {
            var bits = Modulator.modulate(requestExpr);
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/aliens/send"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .POST(HttpRequest.BodyPublishers.ofString(bits))
                    .build();

            var response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            var status = response.statusCode();

            if (status != HttpURLConnection.HTTP_OK) {
                System.out.println("Unexpected server response:");
                System.out.println("HTTP code: " + status);
                System.out.println("Response body: " + response.body());
                System.exit(2);
            }
            System.out.println("Server response: " + response.body());
            return Demodulator.demodulate(response.body());
        } catch (Exception e) {
            System.out.println("Unexpected server response:");
            e.printStackTrace(System.out);
            System.exit(1);
        }
        throw new RuntimeException();
    }

    public static void main(String[] args) throws Exception {
        String serverUrl = args[0];
        long playerKey = Long.parseLong(args[1]);
        boolean isLocalRun = args.length > 2 && args[2] == "local";
        if (!isLocalRun) {
            GalaxyReader.setGalaxyPath("/solution/app/build/galaxy.txt");
        }
        Expr galaxy = GalaxyReader.galaxy();
        Expr req0 = new Pair(
            yuizumi.eval.Number.of(2),
            new Pair(yuizumi.eval.Number.of(playerKey),
            Nil.EXPR));
        Expr res0 = sendQuery(serverUrl, req0);
        System.out.println(res0);
    }
}
