import io.github.cdimascio.dotenv.Dotenv;

public class Helpers {

    public static String getEnvVar(String envVar, String defaultValue) {

        Dotenv dotenv = Dotenv.load();

        String envVarSystem = System.getenv(envVar);
        String envVarEnv    = dotenv.get(envVar);

        String envVarValue = defaultValue;
        // Get context from environment variable PORT, default to 9081 if not set
        if (envVarSystem != null) {
            envVarValue = envVarSystem;
        } else if (envVarEnv != null) {
            envVarValue = envVarEnv;
        } else {
            System.out.println("No " + envVar + " specified in SYSTEM or Env File, Using default context:" + defaultValue);
        }

        return envVarValue;
    }
}
