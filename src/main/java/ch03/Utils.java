package ch03;

public class Utils {
    public static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            System.err.printf("ERROR: Environment variable %s is missing!%n", name);
            System.err.println("   Fix it and run again:");
            System.err.println("   export " + name);
            System.exit(1);
        }
        return value.trim();
    }
}
