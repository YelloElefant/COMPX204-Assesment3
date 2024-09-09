import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TftpWorker {
   private DatagramPacket req;
   private static final byte RRQ = 1;
   private static final byte DATA = 2;
   private static final byte ACK = 3;
   private static final byte ERROR = 4;

   private byte type;
   private byte[] data;
   public String filename;

   public TftpWorker(DatagramPacket req) {
      this.req = req;

      this.type = req.getData()[0];
      this.data = GetDataFromPacket(req);

      filename = new String(data);

      if (this.type != RRQ) {
         System.err.println("Invalid request type");
         return;
      }

   }

   public void run() {

      byte[] fileData = ReadFile(filename);

      List<byte[]> blocks = GetBlocks(fileData);

      sendBlocks(blocks);

   }

   private void sendBlocks(List<byte[]> blocks) {
      // send each block and wait for a response before sending the next

      try {
         DatagramSocket ds = new DatagramSocket(0);

         InetAddress clientAddress = req.getAddress();
         int clientPort = req.getPort();

         for (int i = 0; i < blocks.size(); i++) {
            byte blockNumber = (byte) (i + 1);

            byte[] block = blocks.get(i);

            DatagramPacket packet = MakePacket(DATA, blockNumber, block, clientAddress, clientPort);

            ds.send(packet);

            byte[] ackData = new byte[2];
            DatagramPacket ackPacket = new DatagramPacket(ackData, 2);

            ds.receive(ackPacket);

            byte ackType = ackData[0];
            byte blockNumberClient = ackData[1];

            if (ackType != ACK || blockNumberClient != blockNumber) {
               System.err.println("Invalid ack");
               return;
            }

         }

         byte[] finalPacketData = new byte[2];
         finalPacketData[0] = 5;
         finalPacketData[1] = (byte) (blocks.size() + 1);

         DatagramPacket finalPacket = new DatagramPacket(finalPacketData, 0, finalPacketData.length, clientAddress,
               clientPort);

         ds.send(finalPacket);

      } catch (Exception e) {
         System.err.println("Error sending blocks");
      }
   }

   private byte[] ReadFile(String filename) {
      try {
         File file = new File(filename);
         FileInputStream fis = new FileInputStream(file);
         byte[] fileData = fis.readAllBytes();
         return fileData;
      } catch (Exception e) {
         System.err.println("Error reading file");
         return null;
      }

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

   private static DatagramPacket MakePacket(byte type, byte block, byte[] data, InetAddress address, int port) {
      byte[] packetData = new byte[data.length + 2];
      packetData[0] = type;
      packetData[1] = block;
      System.arraycopy(data, 0, packetData, 2, data.length);

      return new DatagramPacket(packetData, 0, packetData.length, address, port);
   }

}
