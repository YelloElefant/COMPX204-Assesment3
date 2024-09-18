import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.*;

/**
 * This class represents a TFTP packet. This splits out the sections of a TFTP
 * packet. It has a type, block number, and data
 * section. It also has a map of error codes to Exceptions that correspond to
 * the error codes in the TFTP protocol RFC-1350 this is used to get the
 * Exception that corresponds to the error code in an error packet
 * 
 * @see InvalidPacketException
 * @see DatagramPacket
 * @see Exception
 */
public class TftpPacket {
   /**
    * the type of packet (RRQ, DATA, ACK, ERROR)
    */
   private byte type;

   /**
    * the block number of the packet
    */
   private int blockNumber = 512;

   /**
    * the data section of the packet
    */
   private byte[] data;

   /**
    * this is a map of error codes to Exceptions that correspond to the error codes
    * in the TFTP protocol RFC-1350 this is used to get the Exception that
    * corresponds
    * to
    * the
    * error code in an error packet
    */
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
    * @throws InvalidPacketException if the packet is not a valid TFTP packet
    */
   public TftpPacket(DatagramPacket p) throws InvalidPacketException {

      this.type = p.getData()[0];

      if (this.type < 1 || this.type > 5) {
         throw new InvalidPacketException("Invalid packet type");
      }

      int offset = 1;

      if (this.type == 2 || this.type == 3) {
         this.blockNumber = p.getData()[1];
         offset++;
      }

      if (this.type != 3) {
         this.data = Arrays.copyOfRange(p.getData(), offset, p.getLength());
      }

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
    * @throws InvalidPacketException if the packet doesnt contain a block number
    * @see InvalidPacketException
    */
   public byte getBlockNumber() throws InvalidPacketException {
      if (blockNumber == 512) {
         throw new InvalidPacketException("Packet doesnt contain a block number");
      } else {
         return (byte) blockNumber;
      }
   }

   /**
    * Get the data of the packet
    * 
    * @return the data of the packet
    * @throws InvalidPacketException if the packet doesnt contain data
    * @see InvalidPacketException
    */
   public byte[] getData() throws InvalidPacketException {
      if (data == null) {
         throw new InvalidPacketException("Packet doesnt contain data");
      } else {
         return data;
      }
   }
}
