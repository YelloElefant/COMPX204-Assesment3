import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TftpWorker extends Thread {
   private static final byte RRQ = 1;
   private static final byte DATA = 2;
   private static final byte ACK = 3;
   private static final byte ERROR = 4;
   private String name = "TftpWorker";

   private byte type;
   private byte[] data;
   public String filename;

   private DatagramSocket ds;
   private InetAddress clientAddress;
   private int clientPort;
   private int port;

   public int getPort() {
      return port;
   }

   public String getLocalName() {
      return name;
   }

   public TftpWorker(DatagramPacket req, int number) {
      this.name += number;

      this.type = req.getData()[0];
      this.data = GetDataFromPacket(req);

      filename = new String(data);

      try {
         ds = new DatagramSocket();
      } catch (Exception e) {
         out("Error creating socket");
         return;
      }

      clientAddress = req.getAddress();
      clientPort = req.getPort();
      port = ds.getLocalPort();

      if (this.type == ACK) {
         out("ACK found, sending error to client");
         Respond(MakePacket(ERROR, new byte[] { (byte) 8 }, clientAddress, clientPort));
      } else if (this.type != RRQ) {
         out("Invalid request type, dieing...");
         return;
      }

   }

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

   private void Respond(DatagramPacket p) {
      try {
         ds.send(p);
      } catch (Exception e) {
         out("Error sending response");
      }
   }

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
               // try {
               // ds.receive(ackPacket);
               // break;
               // } catch (Exception e) {
               // out("Timeout... closing connection");
               // return;
               // }

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
            out("ACK " + blockNumberClient + " received to block " + blockNumber);

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

   private byte[] ReadFile(String filename) throws Exception {

      File file = new File(filename);
      FileInputStream fis = new FileInputStream(file);
      byte[] fileData = fis.readAllBytes();
      fis.close();
      return fileData;

   }

   private byte[] GetDataFromPacket(DatagramPacket p) {
      return Arrays.copyOfRange(p.getData(), 1, p.getLength());
   }

   private static List<byte[]> GetBlocks(byte[] data) {
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

   private static DatagramPacket MakePacket(byte type, byte[] data, InetAddress address, int port) {
      byte[] packetData = new byte[data.length + 1];
      packetData[0] = type;
      System.arraycopy(data, 0, packetData, 1, data.length);

      return new DatagramPacket(packetData, 0, packetData.length, address, port);
   }

   private static DatagramPacket MakePacket(byte type, byte block, byte[] data, InetAddress address, int port) {
      byte[] packetData = new byte[data.length + 1];
      packetData[0] = block;
      System.arraycopy(data, 0, packetData, 1, data.length);

      return MakePacket(type, packetData, address, port);
   }

   private void out(String s) {
      OutPutStream.out(name + ": " + s);
      // ConsoleWorker.addToSchedual(s);
   }

}
