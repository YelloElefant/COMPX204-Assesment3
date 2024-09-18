import java.net.InetAddress;

public class byteStuff {
   public static void main(String[] args) {

      try {
         InetAddress address = InetAddress.getByName("LAB-RG18-18.cms.waikato.ac.nz");
         System.out.println("IP address: " + address.getHostAddress());

      } catch (Exception e) {
         System.err.println("Exception: " + e);
      }

   }
}
