import java.net.DatagramPacket;

public class TftpWorker {
   private DatagramPacket req;
   private static final byte RRQ = 1;
   private static final byte DATA = 2;
   private static final byte ACK = 3;
   private static final byte ERROR = 4;

   private void sendfile(String filename) {
      /*
       * open the file using a FileInputStream and send it, one block at
       * a time, to the receiver.
       */
      return;
   }

   public void run() {
      /*
       * parse the request packet, ensuring that it is a RRQ
       * and then call sendfile
       */
      return;
   }

   public TftpWorker(DatagramPacket req) {
      this.req = req;
   }
}
