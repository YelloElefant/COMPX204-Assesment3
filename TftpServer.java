import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   private static int port = 69;
   public static List<TftpWorker> workers = new ArrayList<TftpWorker>();

   public synchronized static void editWorkerList(List<TftpWorker> edit) {
      workers = edit;
   }

   public static void main(String[] args) {
      int workerCounter = 0;
      try {
         DatagramSocket ds = new DatagramSocket(port);
         System.out.println("TftpServer on port " + port);

         for (;;) {
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);

            ds.receive(p);

            workerCounter = workers.size();

            TftpWorker worker = new TftpWorker(p, workerCounter++);

            System.out
                  .println(worker.getLocalName() + " - serving " + p.getAddress().getHostAddress() + ":" + p.getPort()
                        + " - on port " + worker.getPort() + " - for file " + worker.filename);

            workers.add(worker);
            worker.start();

         }
      } catch (Exception e) {
         System.err.println("Exception: " + e);
      }

      return;
   }

}
