public class Logger {
    private static boolean enabled = false;

    public static void log(String message) {
        if(enabled) {
            System.out.println(message);
        }
    }
    public static void setEnabled(boolean value) { enabled = value; }

    public static void print(String message) {
        System.out.println(message);
    }
}
