public class OutPutStream {
    private static int lineCount = 0;

    public static void out(String message) {
        System.out.println(message);
        lineCount++;
        System.out.flush();
    }

    public static void moveToTop() {
        System.out.print(String.format("\033[%dA", lineCount));
    }

    public static void clearOutPut() {
        for (int i = 0; i < lineCount; i++) {
            System.out.print(String.format("\033[%dA", 1));
            System.out.print("\033[2K"); // Erase line content
        }
    }
}
