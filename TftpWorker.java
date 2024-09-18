import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The TftpWorker class is a thread that handles a single TFTP request. The
 * worker is created with a DatagramPacket containing the request and an id
 * number. The worker reads the request and extracts the filename from the
 * packet. The worker then reads the file and splits it into blocks of 512 bytes
 * each. The worker then sends each block
 * to the client and waits for an ACK packet with the block number that matches
 * the one it sent. If the client does not respond after 5 seconds the block is
 * resent. If the client does not respond after 30 seconds the connection is
 * closed. EOF is marked by sending a block of size less than 512 or a block of
 * 0.
 * 
 * @author YelloElefant
 * @version 1.0
 * @see DatagramPacket
 * @see DatagramSocket
 * @see InetAddress
 * @see Thread
 */
public class TftpWorker extends Thread {

   /**
    * Request packet, type code 1, the data is the filename to read, NO block
    * number
    */
   private static final byte RRQ = 1;

   /**
    * Data packet, type code 2, contains the block number followed by the data
    * being sent
    */
   private static final byte DATA = 2;

   /**
    * Acknowledgement packet, type code 3, just contains the block number of the
    * data packet being acknowledged
    */
   private static final byte ACK = 3;

   /**
    * Error packet, type code 4, just contains the error code describing the error,
    * NO block number
    */
   private static final byte ERROR = 4;

   /**
    * The name of the worker
    */
   private String name = "TftpWorker";

   /**
    * The name of the file to read
    */
   public String filename;

   /**
    * The DatagramSocket used to send and receive packets
    */
   private DatagramSocket ds;

   /**
    * The address of the client
    */
   private InetAddress clientAddress;

   /**
    * The port of the client
    */
   private int clientPort;

   /**
    * The port the worker is listening on
    */
   private int port;

   /**
    * Returns the port the worker is listening on
    * 
    * @return the port the worker is listening on
    */
   public int getPort() {
      return port;
   }

   /**
    * Returns the name of the worker
    * 
    * @return the name of the worker
    */
   public String getLocalName() {
      return name;
   }

   /**
    * Constructor for the TftpWorker class this sets context for the worker. This
    * constructor takes a DatagramPacket
    * and an id number. The id is appended to the name of the worker to make it
    * easy
    * to identify the worker in the console output. The packet is processed by
    * first getting the type of packet
    * then the filename is extracted from the packet. The type is checked to see if
    * it is a RRQ packet if it is a ACK packet then an ERROR packet is sent to the
    * client with the error code 8. If the type is neither then the worker dies.
    * context is set on the worker by creating a new DatagramSocket with the adress
    * and port
    * of the client from the req packet. the file name is the data from the packet
    * excluding the first byte
    * 
    * @param req    the request packet to process
    * @param number the id number of the worker
    */
   public TftpWorker(DatagramPacket req, int id) throws InvalidPacketException, SocketException, SecurityException {
      TftpPacket request = new TftpPacket(req);
      this.name += id;

      byte type = request.getType();
      filename = new String(request.getData());

      ds = new DatagramSocket();

      clientAddress = req.getAddress();
      clientPort = req.getPort();
      port = ds.getLocalPort();

      if (type == ACK) {
         out("ACK found, sending error to client");
         Respond(MakePacket(ERROR, new byte[] { (byte) 8 }, clientAddress, clientPort));
      } else if (type != RRQ) {
         out("Invalid request type, dieing...");
         return;
      }

   }

   /**
    * The run method for the worker thread executes when the thread starts. simply
    * reads the file, gets the blocks
    * from the file data, then calls {@link #sendBlocks(List)} to
    * send the blocks to the client
    */
   public void run() {

      byte[] fileData;

      try {
         fileData = ReadFile(filename);
      } catch (FileNotFoundException e) {
         out("File not found");
         Respond(MakePacket(ERROR, new byte[] { (byte) 1 }, clientAddress, clientPort));
         return;
      } catch (Exception e) {
         out("Error reading file");
         Respond(MakePacket(ERROR, "Error reading file".getBytes(), clientAddress, clientPort));
         return;
      }

      List<byte[]> blocks = GetBlocks(fileData);

      sendBlocks(blocks);

   }

   /**
    * Sends a packet to the client. This method sends the given packet to the
    * client and catches any exceptions that may occur while sending the packet
    * this method is a helper method for the thread to be called at anypoint after
    * initialization to send any spacket to the client
    *
    * @param p the packet to send to the client
    * @see DatagramPacket
    */
   private void Respond(DatagramPacket p) {
      try {
         ds.send(p);
      } catch (Exception e) {
         out("Error sending response");
      }
   }

   /**
    * Sends a list of blocks to the client. This method sends each block in the
    * list to the client and waits for an ACk packet with a block number that
    * matches the one it sent, before sending the next block. If the
    * client does not respond after 5 seconds the block is resent. If the client
    * does not respond after 30 seconds the connection is closed.
    * all blocks are sent in order and the last block is marked as the last block
    * by either
    * if the last block in the list is of size 512 then a final packet is sent with
    * length 0
    * if the last block is less than 512 then that is the final packet sent
    *
    * @param blocks the list of byte arrays to send to the client
    */
   private void sendBlocks(List<byte[]> blocks) {
      // send each block and wait for a response before sending the next

      try {

         for (int i = 0; i < blocks.size(); i++) {
            byte blockNumber = (byte) (i + 1);

            byte[] block = blocks.get(i);

            DatagramPacket packet = MakePacket(DATA, blockNumber, block, clientAddress, clientPort);

            Respond(packet);
            if (block.length < 512) {
               out("Last block sent");
               return;
            }
            byte[] ackData = new byte[2];
            DatagramPacket ackPacket = new DatagramPacket(ackData, 2);

            int acksTimeOut = 0;
            ds.setSoTimeout(5000);

            for (;;) {

               try {
                  ds.receive(ackPacket);
                  acksTimeOut = 0;
                  break;
               } catch (SocketTimeoutException e) {
                  acksTimeOut++;
                  System.out.println("Client not responding... retrying data " + blockNumber);
                  if (acksTimeOut == 6) {
                     System.out.println("Client not responding closing conection");
                     return;
                  }
                  Respond(MakePacket(DATA, blockNumber, block, clientAddress, clientPort));
               }
            }

            byte ackType = ackData[0];
            byte blockNumberClient = ackData[1];

            if (ackType != ACK) {
               out("Invalid ack");
               return;
            }
            if (blockNumberClient != blockNumber) {
               i = blockNumberClient;
            }
         }

         byte[] finalPacketData = new byte[3];
         finalPacketData[0] = DATA;
         finalPacketData[1] = (byte) (blocks.size() + 1);
         finalPacketData[2] = 0;

         DatagramPacket finalPacket = new DatagramPacket(finalPacketData, 0, finalPacketData.length, clientAddress,
               clientPort);

         Respond(finalPacket);
         out("All blocks sent");
         ds.close();
      } catch (Exception e) {
         out("Error sending blocks");
      }
   }

   /**
    * Reads a file from a specified filename (this is complete path to file) and
    * returns the data as a byte array
    *
    * @param filename the path to the file to read
    * @return a byte array containing the data from the file
    * @throws Exception if there is an error reading the file
    * @see File
    * @see FileInputStream
    */
   private byte[] ReadFile(String filename) throws Exception {

      File file = new File(filename);
      FileInputStream fis = new FileInputStream(file);
      byte[] fileData = fis.readAllBytes();
      fis.close();
      return fileData;

   }

   /**
    * Splits a byte[] into blocks of 512 bytes each and returns a list of byte
    * arrays each of size 512 except the last one which may be smaller than or
    * equil to 512
    * 
    * @param data the byte[] to split into blocks
    * @return a list of byte arrays
    */
   private List<byte[]> GetBlocks(byte[] data) {
      List<byte[]> blocks = new ArrayList<byte[]>();

      int amountOfBlocks = data.length / 512;

      int start = 0;
      int end = 512;
      byte[] block;

      while (end / 512 != amountOfBlocks + 1) {
         block = Arrays.copyOfRange(data, start, end);
         blocks.add(block);
         start += 512;
         end += 512;

      }
      block = Arrays.copyOfRange(data, start, data.length);
      blocks.add(block);

      return blocks;

   }

   /**
    * Creates a packet with the given type, data, address and port. This method
    * takes the data array and shifts it down by one and the
    * type is slotted into the first position. The packet is then created with the
    * given data, address and port
    * 
    * @param type    the type of packet to create (RRQ, DATA, ACK, ERROR)
    * @param data    the data to send in the packet
    * @param address the address to send the packet to
    * @param port    the port to send the packet to
    * @return a {@link DatagramPacket DatagramPacket} with the given type, data,
    *         address and port
    * @see DatagramPacket
    */
   private DatagramPacket MakePacket(byte type, byte[] data, InetAddress address, int port) {
      byte[] packetData = new byte[data.length + 1];
      packetData[0] = type;
      System.arraycopy(data, 0, packetData, 1, data.length);

      return new DatagramPacket(packetData, 0, packetData.length, address, port);
   }

   /**
    * Creates a packet with the given type, block number, data, address and port.
    * this method calls the MakePacket method with the data array shifted down by
    * one and the block number as the first byte
    * then is passed to the other MakePacket method to create the packet
    * 
    * 
    * @param type    the type of packet to create (RRQ, DATA, ACK, ERROR)
    * @param block   the block number of the packet
    * @param data    the data to send in the packet
    * @param address the address to send the packet to
    * @param port    the port to send the packet to
    * @return a DatagramPacket with the given type, block number, data, address and
    *         port
    * @see MakePacket(byte, byte[], InetAddress, int)
    * @see DatagramPacket
    */
   private DatagramPacket MakePacket(byte type, byte block, byte[] data, InetAddress address, int port) {
      byte[] packetData = new byte[data.length + 1];
      packetData[0] = block;
      System.arraycopy(data, 0, packetData, 1, data.length);

      return MakePacket(type, packetData, address, port);
   }

   /**
    * Outputs a message to the console with the name of the worker at the start
    * 
    * @param s The message to output
    */
   private void out(String s) {
      System.out.println(name + ": " + s);
   }

}
