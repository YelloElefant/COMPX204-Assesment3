import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.*;

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
         7, new Exception("No such user."),
         8, new Exception("ACK violation."));

   /**
    * this gets the Exception from the error code in an error packet
    *
    * @return the Exception that corresponds to the error code
    * @throws InvalidPacketException if the packet is not a valid error packet
    * @see InvalidPacketException
    */
   public Exception getError() throws InvalidPacketException {
      if (data.length == 1) {
         return errorCodes.get((int) data[0]);
      }
      throw new InvalidPacketException("Invalid error packet");

   }

   /**
    * Constructor for a TFTP packet this takes a DatagramPacket and extracts the
    * type, block number (if it exists) and data
    * 
    * @param p the DatagramPacket to extract the data from
    * @see DatagramPacket
    */
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

   /**
    * gets the type of packet
    * 
    * @return the type of packet
    */
   public byte getType() {
      return type;
   }

   /**
    * Get the block number of the packet
    * 
    * @return the block number of the packet
    */
   public byte getBlockNumber() {
      return blockNumber;
   }

   /**
    * Get the data of the packet
    * 
    * @return the data of the packet
    */
   public byte[] getData() {
      return data;
   }
}
