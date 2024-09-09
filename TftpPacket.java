import java.net.DatagramPacket;
import java.util.Arrays;

public class TftpPacket {
   private byte type;
   private byte blockNumber;
   private byte[] data;

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
