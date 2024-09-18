import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   private static int port = 69;
   public static List<TftpWorker> workers = new ArrayList<TftpWorker>();

   public synchronized static void editWorkerList(List<TftpWorker> edit) {
      workers = edit;
   }

   /**
    * This is the main method for the TftpServer. It binds a new DatagramSocket to
    * port 69 and listens for incoming packets. When a packet is received, a new
    * TftpWorker is created to handle the packet and respond accordingly.
    * whne a worker is created, it is added to the workers list and prints out the
    * worker's information (Workers name, who it is servering and to what port,
    * what port it is serving on and what file it is serving).
    *
    * @param args
    */
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

            try {
               TftpWorker worker = new TftpWorker(p, workerCounter++);

               System.out.println(
                     worker.getLocalName() + " - serving " + p.getAddress().getHostAddress() + ":" + p.getPort()
                           + " - on port " + worker.getPort() + " - for file " + worker.filename);

               workers.add(worker);
               worker.start();
            } catch (Exception e) {
               System.err.println("Error creating worker: " + e);
            }

         }
      } catch (Exception e) {
         System.err.println("Exception: " + e);
      }

   }

}
