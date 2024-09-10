import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   private static int port = 69;

   public void start_server() {
      int workerCounter = 0;
      try {
         DatagramSocket ds = new DatagramSocket(port);
         System.out.println("TftpServer on port " + port);

         for (;;) {
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);
            OutPutStream.clear();
            OutPutStream.out("Worker Count: " + workerCounter);

            ds.receive(p);

            TftpWorker worker = new TftpWorker(p, workerCounter++);
            // OutPutStream.out("Worker created - for " +
            // p.getAddress().toString().substring(1) + ":" + p.getPort()
            // + " - requesting "
            // + new String(worker.filename));
            worker.run();

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
