import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class send {
    public static void main(String[] args) {
        try {
            DatagramSocket ds = new DatagramSocket(68);

            byte[] data = "test.png".getBytes();

            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            DatagramPacket packet = new DatagramPacket(data, 0, data.length, serverAddress, 69);

            ds.send(packet);

        } catch (Exception e) {

        }

    }
}
