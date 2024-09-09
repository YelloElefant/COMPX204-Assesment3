import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   private static int port = 69;

   public void start_server() {
      try {
         DatagramSocket ds = new DatagramSocket(port);
         System.out.println("TftpServer on port " + port);

         for (;;) {
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);
            ds.receive(p);

            TftpWorker worker = new TftpWorker(p);
            worker.run();

         }
      } catch (Exception e) {
         System.err.println("Exception: " + e);
      }

      return;
   }

   private byte[] GetDataFromPacket(DatagramPacket p) {
      return Arrays.copyOfRange(p.getData(), 0, p.getLength());
   }

   public static void main(String args[]) {
      TftpServer d = new TftpServer();
      d.start_server();
   }
}
