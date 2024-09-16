import java.io.*;
import java.net.*;
import java.util.*;
// import java.util.Arrays;

// TODO buffer data then check next data packet and if they are the same write one if they are differnt write the previouse one

public class TftpClient {

    private static DatagramSocket ds;
    private static int port = 69;
    private static InetAddress serverAddress;
    private static int serverPort = 0;
    private static String filename;
    private static String saveLocation = "received_";
    private static byte[] repsonseBuffer;

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
                saveLocation += filename.split("/")[filename.split("/").length - 1];
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

            byte responseBlockNumber = 0;
            byte prevBlockNumber = -1;

            int temp = 0;

            // get response
            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.setSoTimeout(30000);

                for (;;) {
                    try {
                        ds.receive(p);
                        // check if server port is set
                        if (serverPort == 0) {
                            serverPort = p.getPort();
                        }
                        break;
                    } catch (SocketTimeoutException e) {
                        System.out.println("Server not responding... closing conection");
                        return;
                    }
                }

                responseBlockNumber = HandleResponse(p);
                if (responseBlockNumber != prevBlockNumber) {
                    WriteToFile(repsonseBuffer);
                    prevBlockNumber = responseBlockNumber;
                } else {
                    System.out.println("Duplicate block received not writing to file");
                }

                // if (responseBlockNumber == -1 ) {
                // System.out.println("Error packet received");
                // return;
                // }

                // send back ACK
                DatagramPacket ackPacket = new DatagramPacket(new byte[] { 0, responseBlockNumber }, 2, serverAddress,
                        serverPort);

                if (temp == 100) {
                    temp = 0;
                } else {
                    Acknowledge(ackPacket);
                    System.out
                            .println("ACK " + responseBlockNumber + " sent to: " + p.getAddress() + ":" + p.getPort());
                    temp++;
                }

            }

        } catch (Exception e) {
            System.err.println("Exception: " + e.getStackTrace().toString());

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
        repsonseBuffer = handledPacket.getData();

        System.out.println("Block " + responseBlockNumber + " received");

        // write each response block to a file

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

    private static void WriteToFile(byte[] data) {
        FileOutputStream fos = null;

        try {
            File file = new File(saveLocation);
            fos = new FileOutputStream(file, true);
            fos.write(data);
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
    }
}
