import org.apache.hc.client5.http.classic.methods.HttpPost;
//import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ContentType;

import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleTriplyUpload {

    static void main(String[] args) throws Exception {

        Path filePath = Paths.get("C:", "Users", "Ronald Top", "Downloads", "test.ttl");
        byte[] turtleBytes = Files.readAllBytes(filePath);
        String turtleContent = new String(turtleBytes, StandardCharsets.UTF_8);

        uploadToTriplyDB(turtleContent);

    }
    public static String uploadToTriplyDB(String turtleContent) throws Exception {

        String uploadUrl = Helpers.getEnvVar("TRIPLYDB_UPLOAD_URL", null);
        String apiToken = Helpers.getEnvVar("TRIPLYDB_API_TOKEN", null);

        if (uploadUrl == null || apiToken == null) {
            String msg = "Missing TRIPLYDB_UPLOAD_URL and TRIPLYDB_API_TOKEN environment variables.";
            System.err.println(msg);
            return msg;
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file",                             // must be "file"
                turtleContent.getBytes(StandardCharsets.UTF_8),
                ContentType.create("text/turtle"),
                "test2.ttl"                               // bogus, but necessary, will appear in logs
        );
        HttpEntity entity = builder.build();

        // --- Preview first 15 lines ---
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.writeTo(out);
        String[] lines = out.toString(StandardCharsets.UTF_8).split("\r?\n");
        for (int i = 0; i < Math.min(lines.length, 15); i++) System.out.println(lines[i]);
        System.out.println("--- End preview ---\n");

        // --- Send POST to TriplyDB ---
        HttpPost post = new HttpPost(uploadUrl);
        post.setHeader("Authorization", "Bearer " + apiToken);
        post.setEntity(entity);

        System.out.println("creating default client");
        String response;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            response = client.execute(post, resp -> EntityUtils.toString(resp.getEntity()));
        }
        System.out.println("TriplyDB response: " + response);
        return response;
    }
}
