import java.io.*;
import java.net.*;
import java.util.*;
// import java.util.Arrays;

public class TftpClient {

    private static DatagramSocket ds;
    private static int port = 69;
    private static InetAddress serverAddress;
    private static String filename;
    private static String saveLocation = "received_";

    public static void main(String[] args) {
        try {
            if (args.length > 2 || args.length < 1) {
                System.err.println("Usage: java TftpClient <filename>");
                System.exit(1);
            }

            // get file name
            filename = args[0];
            if (args.length == 2) {
                saveLocation = args[1];
            } else {
                saveLocation += filename;
            }

            System.out.println("Requesting file: " + filename);
            System.out.println("Save location: " + saveLocation);

            ds = new DatagramSocket();

            byte[] data = filename.getBytes();
            byte type = 1;
            byte[] message = new byte[data.length + 1];
            message[0] = type;
            System.arraycopy(data, 0, message, 1, data.length);

            serverAddress = InetAddress.getByName("127.0.0.1");

            DatagramPacket packet = new DatagramPacket(message, 0, message.length, serverAddress, port);

            ds.send(packet);
            int acksTimeOut = 0;

            // get response
            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.setSoTimeout(5000);

                for (;;) {
                    try {
                        ds.receive(p);
                        break;
                    } catch (SocketTimeoutException e) {
                        acksTimeOut++;
                        System.out.println("Server not responding... retrying acknoledgement");
                        if (acksTimeOut == 6) {
                            System.out.println("Server not responding");
                            return;
                        }
                        Respond(packet);
                    }
                }

                byte responseBlockNumber = HandleResponse(p);
                if (responseBlockNumber == -1) {
                    System.out.println("Error packet received");
                    return;
                }

                // send back ACK
                DatagramPacket ackPacket = new DatagramPacket(new byte[] { 0, responseBlockNumber }, 2, p.getAddress(),
                        p.getPort());

                Acknowledge(ackPacket);

            }

        } catch (Exception e) {
            System.err.println("Exception nigger: " + e.getStackTrace().toString());

        }

    }

    private static byte HandleResponse(DatagramPacket p) {
        // convert response to string
        TftpPacket handledPacket = new TftpPacket(p);
        byte reponseType = handledPacket.getType();

        // check for error packet
        try {
            if (reponseType == 4) {
                throw handledPacket.getError();
            }
        } catch (InvalidPacketException e) {
            return -1;
        } catch (Exception e) {
            System.err.println("Error packet recived: " + e.getMessage());
            return -1;
        }

        // exit if all blocks received
        if (reponseType == 5) {
            System.out.println("All blocks received");
            System.exit(0);
        }

        byte responseBlockNumber = handledPacket.getBlockNumber();
        byte[] responseBlockData = handledPacket.getData();

        // write each response block to a file
        FileOutputStream fos = null;
        try {
            File file = new File(saveLocation);
            fos = new FileOutputStream(file, true);
            fos.write(responseBlockData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e2) {
                System.err.println("Exception2: " + e2);
            }
        }

        // return with the block number to acknowledge
        return responseBlockNumber;
    }

    private static void Acknowledge(DatagramPacket p) {
        try {
            byte[] ackData = new byte[2];
            ackData[0] = 3;
            ackData[1] = p.getData()[1];
            DatagramPacket ackPacket = new DatagramPacket(ackData, 2, p.getAddress(), p.getPort());
            Respond(ackPacket);
        } catch (Exception e) {
            System.err.println("Error sending ACK");
        }
    }

    private static void Respond(DatagramPacket p) {
        try {
            ds.send(p);
        } catch (Exception e) {
            System.err.println("Error sending response");
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
