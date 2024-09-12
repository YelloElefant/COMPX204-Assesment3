
public class ConsoleWorker extends Thread {
    private String name;

    public ConsoleWorker() {
        this.name = "ConsoleWorker";
    }

    @Override
    public void run() {
        while (true) {
            OutPutStream.clear();
            for (TftpWorker worker : TftpServer.workers) {
                OutPutStream.out("Worker " + worker.getLocalName() + " - " + worker.filename + " - "
                        + worker.getPort());
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    OutPutStream.out("Error sleeping");
                }
            }
        }
    }
}
