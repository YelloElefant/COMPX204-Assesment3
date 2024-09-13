import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   private static int port = 69;
   public static List<TftpWorker> workers = new ArrayList<TftpWorker>();

   public synchronized static void editWorkerList(List<TftpWorker> edit) {
      workers = edit;
   }

   public void start_server() {
      int workerCounter = 0;
      try {
         DatagramSocket ds = new DatagramSocket(port);
         System.out.println("TftpServer on port " + port);

         ConsoleWorker consoleWorker = new ConsoleWorker();
         consoleWorker.start();

         for (;;) {
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);

            ds.receive(p);

            // List<TftpWorker> workersToRemove = new ArrayList<TftpWorker>();
            // for (TftpWorker worker : workers) {
            // if (!worker.isAlive()) {
            // workersToRemove.add(worker);
            // }
            // }
            // workers.removeAll(workersToRemove);
            workerCounter = workers.size();

            TftpWorker worker = new TftpWorker(p, workerCounter++);

            // OutPutStream.out("Worker created - for " +
            // p.getAddress().toString().substring(1) + ":" + p.getPort()
            // + " - requesting "
            // + new String(worker.filename));

            workers.add(worker);
            worker.start();

            // OutPutStream.clear();
            // for (TftpWorker workerLog : workers) {
            // OutPutStream.out("Worker " + workerLog.getLocalName() + " - " +
            // workerLog.filename + " - "
            // + workerLog.getPort());
            // }

         }
      } catch (Exception e) {
         System.err.println("Exception: " + e);
      }

      return;
   }

   public static void main(String args[]) {
      TftpServer d = new TftpServer();
      d.start_server();
   }
}
