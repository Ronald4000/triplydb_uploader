import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class SparqlUploadGateway2 {

    public static void main(String[] args) throws IOException {

        String port = "9081";
        String context = "/upload";

        port = Helpers.getEnvVar("PORT", port);
        context = Helpers.getEnvVar("CONTEXT", context);

        int portInt;

        try {
            portInt = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid PORT SYSTEM environment variable");
            return;
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(portInt), 0);
        server.createContext(context, new UploadHandler());
        server.setExecutor(null);
        System.out.println("Server started at http://localhost:" + port + context);
        server.start();
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String responseText;
            int responseCode;
            String payload=null;

            try {
                // Read full incoming request body
                System.out.println("received POST: Reading All Bytes");

                String contentType = exchange.getRequestHeaders().getFirst("Content-type");
                String boundary = contentType.split("boundary=")[1];

                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                // Split into parts
                String[] parts = body.split("--" + boundary);

                // Find the file part (the part containing "filename=")
                for (String part : parts) {
                    if (part.contains("filename=")) {
                        // Remove headers: everything before the first blank line
                        payload = part.substring(part.indexOf("\r\n\r\n") + 4);

                        // Remove trailing boundary CRLF
                        payload = payload.trim();

                        System.out.println("Payload = " + payload);
                    }
                }

                String message = payload;
                System.out.println("inside POST: Read All Bytes");
                // Transform message (replace with your real logic)
                System.out.println("transforming insert data {} to TTL");
                String turtleContent = SparqlToTurtleConverter.convertString(message);
                System.out.println("Message transformed to TTL");
                // Upload synchronously to TriplyDB
                System.out.println("Trying to upload to TriplyDB endpoint");
                responseText = SimpleTriplyUpload.uploadToTriplyDB(turtleContent);
                responseCode = 200;

            } catch (Exception e) {
                e.printStackTrace();
                responseText = "Error during upload: " + e.getMessage();
                responseCode = 500;
            }

            // Send response back to client
            byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
