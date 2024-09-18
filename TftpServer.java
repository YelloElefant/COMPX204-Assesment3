import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class TftpServer {
   /**
    * This is the port that the TftpServer listens on. This is set to 69 by default
    * as this is the port that TFTP servers listen on.
    */
   private static int port = 69;

   /**
    * This is a list of all the workers that are currently running. This is used to
    * keep track of all the workers that are running.
    */
   public static List<TftpWorker> workers = new ArrayList<TftpWorker>();

   /**
    * This is the main method for the TftpServer. It binds a new DatagramSocket to
    * port 69 and listens for incoming packets. When a packet is received, a new
    * TftpWorker is created to handle the packet and respond accordingly.
    * whne a worker is created, it is added to the workers list and prints out the
    * worker's information (Workers name, who it is servering and to what port,
    * what port it is serving on and what file it is serving).
    *
    * @param args arguments to the program. If there is a port number in position 1
    *             in the args, it will use that port number
    */
   public static void main(String[] args) {
      // if there is a port number in the arguments, use that port number
      if (args.length > 0) {
         port = args[0].equals("") ? 69 : Integer.parseInt(args[0]);
      }

      int workerCounter = 0;

      // create a new DatagramSocket to listen on the port number
      try {
         DatagramSocket ds = new DatagramSocket(port);
         System.out.println("TftpServer on port " + port);

         // loop forever listening for packets and creating workers to handle them
         for (;;) {
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);

            // receive a packet
            ds.receive(p);

            workerCounter = workers.size();

            // create a new worker to handle the packet received
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
