import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileReader;
import java.io.IOException;

public class SparqlToTurtleConverter {

    public static String convertString(String sparqlContent) throws IOException {
        StringBuilder turtleContent = new StringBuilder();

        // Use StringReader to treat the input String like a file/stream
        try (BufferedReader reader = new BufferedReader(new StringReader(sparqlContent))) {

            String line;
            boolean inDataBlock = false;

            while ((line = reader.readLine()) != null) {
                // Trim leading/trailing whitespace for reliable matching
                String trimmedLine = line.trim();

                // 1. Detect the start of the data block
                if (trimmedLine.startsWith("INSERT DATA {")) {
                    inDataBlock = true;
                    // Skip the wrapper line
                    continue;
                }

                // 2. Detect the end of the data block
                if (inDataBlock && trimmedLine.equals("}")) {
                    inDataBlock = false;
                    // Skip the wrapper line and stop processing
                    break;
                }

                // 3. Process lines outside the data block (prefixes)
                if (!inDataBlock) {
                    // Lines outside the block (like PREFIX) should be copied.
                    if (trimmedLine.startsWith("PREFIX")) {
                        // Replace 'PREFIX name:' with '@prefix name' and add a period if missing.
                        String turtlePrefix = trimmedLine.replaceFirst("PREFIX\\s+", "@prefix ").trim();
                        if (!turtlePrefix.endsWith(".")) {
                            turtlePrefix += " .";
                        }
                        turtleContent.append(turtlePrefix).append("\n");
                    } else if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                        // Copy other non-empty, non-wrapper lines (e.g., comments or base URI)
                        turtleContent.append(line).append("\n");
                    }
                }

                // 4. Process lines inside the data block (the actual triples)
                if (inDataBlock) {
                    // Copy the triple data directly
                    turtleContent.append(line).append("\n");
                }
            }
        }

        return turtleContent.toString().trim();
    }
}