import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// import java.util.Arrays;

public class TftpClient {
    public static void main(String[] args) {
        try {
            // if (args.length != 1) {
            // System.err.println("Usage: java TftpClient <filename>");
            // System.exit(1);
            // }

            // // get file name
            // String filename = args[0];
            String filename = "lol";

            DatagramSocket ds = new DatagramSocket();

            byte[] data = filename.getBytes();
            byte type = 1;
            byte[] message = new byte[data.length + 1];
            message[0] = type;
            System.arraycopy(data, 0, message, 1, data.length);

            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            DatagramPacket packet = new DatagramPacket(message, 0, message.length, serverAddress, 69);

            ds.send(packet);

            // get response
            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);

                // convert response to string
                TftpPacket handledPacket = new TftpPacket(p);

                byte reponseType = handledPacket.getType();

                try {
                    if (reponseType == 4) {
                        throw handledPacket.getError();
                    }
                } catch (InvalidPacketException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Error packet recived: " + e.getMessage());
                    break;
                }

                if (reponseType == 5) {
                    System.out.println("All blocks received");
                    break;
                }

                byte reponseBlockNumber = handledPacket.getBlockNumber();
                byte[] reponseBlockData = handledPacket.getData();

                // write each response block to a file
                try {
                    File file = new File("received_" + filename);
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(reponseBlockData);
                    fos.close();
                } catch (Exception e) {
                    System.err.println("Exception: " + e);
                }

                // send back ACK
                byte[] ackData = new byte[2];
                ackData[0] = 3;
                ackData[1] = reponseBlockNumber;
                DatagramPacket ackPacket = new DatagramPacket(ackData, 2, p.getAddress(), p.getPort());
                ds.send(ackPacket);

            }
            ds.close();

        } catch (Exception e) {
            System.err.println("Exception: " + e.getStackTrace());

        }

    }

    // private static byte HandleResponse(DatagramPacket p) {
    // byte[] data = p.getData();
    // // byte type = data[0];
    // byte blockNumber = data[1];
    // byte[] blockData = Arrays.copyOfRange(data, 2, p.getLength());

    // // print each block
    // System.out.println(new String(blockData));
    // return blockNumber;
    // }
}
