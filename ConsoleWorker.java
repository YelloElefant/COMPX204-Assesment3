import java.io.OutputStream;
import java.util.*;

public class ConsoleWorker extends Thread {
    private String name;
    private static List<String> outSchedual = new ArrayList<>();

    public ConsoleWorker() {
        this.name = "ConsoleWorker";
    }

    public synchronized static void addToSchedual(String message) {
        outSchedual.add(message);
    }

    @Override
    public void run() {
        while (true) {

            List<TftpWorker> workersToRemove = new ArrayList<TftpWorker>();
            for (TftpWorker worker : TftpServer.workers) {
                if (!worker.isAlive()) {
                    workersToRemove.add(worker);
                }
            }
            TftpServer.workers.removeAll(workersToRemove);

            for (TftpWorker worker : TftpServer.workers) {

                OutPutStream.out("Worker " + worker.getLocalName() + " - " + worker.filename + " - "
                        + worker.getPort());
            }

            for (String string : outSchedual) {
                OutPutStream.out(string);
            }
            // OutPutStream.out(OutPutStream.lineCount + "");
            outSchedual.clear();

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                OutPutStream.out("Error sleeping");
            }
            OutPutStream.clear();

        }
    }
}
