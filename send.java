import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class send {
    public static void main(String[] args) {
        try {
            DatagramSocket ds = new DatagramSocket();

            byte[] data = "test".getBytes();
            byte type = 1;
            byte[] message = new byte[data.length + 1];
            message[0] = type;
            System.arraycopy(data, 0, message, 1, data.length);

            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            DatagramPacket packet = new DatagramPacket(message, 0, message.length, serverAddress, 69);

            ds.send(packet);

            // get response
            byte[] buf = new byte[1472];
            DatagramPacket p = new DatagramPacket(buf, 1472);
            ds.receive(p);

            // convert response to string
            byte[] data2 = p.getData();
            String response = new String(data2, 2, p.getLength());
            System.out.println(response);

            // send back ACK
            byte[] ackData = new byte[2];
            ackData[0] = 3;
            ackData[1] = 1;
            DatagramPacket ackPacket = new DatagramPacket(ackData, 2, p.getAddress(), p.getPort());
            ds.send(ackPacket);

        } catch (Exception e) {

        }

    }
}
