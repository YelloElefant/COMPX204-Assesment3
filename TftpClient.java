import java.io.*;
import java.net.*;
import java.util.*;
// import java.util.Arrays;

// TODO: fix logging
// TODO: fix error handling make it fix it self
// TODO: clean up all code
// TODO: first arg change to server:port/fileName

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
                System.err.println("Usage: java TftpClient <filename> <save location>");
                System.exit(1);
            }

            int indexOfStartOfFilePath = args[0].indexOf("/");
            if (indexOfStartOfFilePath < 0) {
                System.out.println("Invalid input");
                return;
            }

            String filePath = args[0].substring(indexOfStartOfFilePath);
            String serverAndPort = args[0].substring(0, indexOfStartOfFilePath);

            String[] serverAndPortSplit = serverAndPort.split(":");
            String server = serverAndPortSplit.length != 1 ? serverAndPortSplit[0] : "localhost";
            port = Integer.parseInt(serverAndPortSplit.length > 1 ? serverAndPortSplit[1].split("/")[0] : "69");

            int fileNameStartIndex = filePath.lastIndexOf("/") + 1;
            String dir = fileNameStartIndex > 1 ? filePath.substring(1, fileNameStartIndex) : "";
            String filename = fileNameStartIndex > 0 ? filePath.substring(fileNameStartIndex) : "";

            serverAddress = InetAddress.getByName(server);
            if (serverAddress == null) {
                System.err.println("Invalid server address");
                System.exit(1);
            }

            System.out.println("Sever: " + server);
            System.out.println("Port: " + port);
            System.out.println("Directory: " + dir);
            System.out.println("Filename: " + filename);

            // set save location
            if (args.length == 2) {
                saveLocation = args[1];
            } else {
                saveLocation += filename;
            }

            // tell user what file is being requested and where it is being saved
            System.out.println("Requesting file: " + filename);
            System.out.println("Requesting file path: " + dir);
            System.out.println("Save location: " + saveLocation);
            System.out.println("Server: " + server);
            System.out.println("Port: " + port);

            // create socket to file requsting server
            ds = new DatagramSocket();

            // create request message
            byte[] data = filename.getBytes();
            byte type = 1;
            byte[] message = new byte[data.length + 1];
            message[0] = type;
            System.arraycopy(data, 0, message, 1, data.length);

            DatagramPacket packet = new DatagramPacket(message, 0, message.length, serverAddress, port);

            ds.send(packet);

            byte responseBlockNumber = 0;
            byte prevBlockNumber = -1;

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

                TftpPacket handeledPacket = new TftpPacket(p);
                responseBlockNumber = HandleResponse(handeledPacket);
                if (responseBlockNumber != prevBlockNumber) {
                    WriteToFile(repsonseBuffer);
                    prevBlockNumber = responseBlockNumber;
                } else {
                    System.out.println("Duplicate block received not writing to file");
                }

                // send back ACK
                DatagramPacket ackPacket = new DatagramPacket(new byte[] { 0, responseBlockNumber }, 2, serverAddress,
                        serverPort);

                Acknowledge(ackPacket);
                System.out
                        .println("ACK " + responseBlockNumber + " sent to: " + p.getAddress() + ":" + p.getPort());

            }

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());

        }

    }

    private static byte HandleResponse(TftpPacket p) {
        // convert response to string
        byte reponseType = p.getType();

        // check for error packet
        try {
            if (reponseType == 4) {
                throw p.getError();
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

        byte responseBlockNumber = p.getBlockNumber();
        repsonseBuffer = p.getData();

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