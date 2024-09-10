import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.*;
import java.io.*;

public class TftpPacket {
   private byte type;
   private byte blockNumber;
   private byte[] data;

   private static Map<Integer, Exception> errorCodes = Map.of(
         0, new Exception("Not defined, see error message (if any)."),
         1, new Exception("File not found."),
         2, new Exception("Access violation."),
         3, new Exception("Disk full or allocation exceeded."),
         4, new Exception("Illegal TFTP operation."),
         5, new Exception("Unknown transfer ID."),
         6, new Exception("File already exists."),
         7, new Exception("No such user."));

   public Exception getError() throws InvalidPacketException {
      if (data.length == 1) {
         return errorCodes.get((int) data[0]);
      }
      throw new InvalidPacketException("Invalid error packet");

   }

   public TftpPacket(DatagramPacket p) {

      this.type = p.getData()[0];

      int offset = 1;

      if (this.type == 2 || this.type == 3) {
         this.blockNumber = p.getData()[1];
         offset++;
      }

      this.blockNumber = p.getData()[1];
      this.data = Arrays.copyOfRange(p.getData(), offset, p.getLength());

   }

   public byte getType() {
      return type;
   }

   public byte getBlockNumber() {
      return blockNumber;
   }

   public byte[] getData() {
      return data;
   }
}
