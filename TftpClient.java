import java.io.*;
import java.net.*;
import java.util.*;
// import java.util.Arrays;

// TODO: fix logging
// TODO: fix error handling make it fix it self
// TODO: clean up all code
// TODO: first arg change to server:port/fileName

/**
 * This class is the TftpClient program. It is used to request a file from a
 * TftpServer using the TFTP protocol. It takes the server address, port, file
 * path
 * and save location as arguments. It then creates a socket to the server and
 * sends
 * a request for the file. It then waits for a response from the server and
 * handles
 * the response. It then responds to the destination and port of the reviced
 * packet.
 * 
 * the client recives the file in blocks of 512 bytes and writes the data to the
 * file
 * in the order it is recived. If the block number is the same as the previous
 * block number
 * the data is not written to the file as it is a duplicate.
 * 
 * The arguments should be in the format server:port/filePath save
 * location
 * 
 * </br>
 * </br>
 * <dt>Default values:</dt>
 * <ul>
 * <li>server: loaclhost</li>
 * <li>port: 69</li>
 * <li>save location: received_ + fileRequested</li>
 * </ul>
 * 
 * 
 * 
 * @author YelloElefant
 * @version 1.0
 */
public class TftpClient {

    /**
     * The DatagramSocket used to send and receive packets to and from the server.
     * This is the DatagramSocket conected to the TftpWorker on the server that is
     * sending the data NOT the server itself.
     * 
     * @see DatagramSocket
     * @see TftpWorker
     */
    private static DatagramSocket ds;

    /**
     * The port number of the server. This is the port number of the TftpServer on
     * the server NOT the TftpWorker.
     * 
     * @see TftpServer
     */
    private static int port = 69;

    /**
     * The InetAddress of the server. This is the InetAddress of the TftpServer on
     * the server NOT the TftpWorker.
     * 
     * @see TftpServer
     * @see InetAddress
     */
    private static InetAddress serverAddress;

    /**
     * The port number of the server. This is the port number of the TftpWorker that
     * is sending the data on the server NOT the TftpServer.
     */
    private static int serverPort = 0;

    /**
     * The complete path to the file to be requested from the server.
     */
    private static String filename;

    /**
     * The location to save the file to. This is the location on the client machine
     * where the file will be saved.
     * 
     * this initilised as "received_" that way if a save loaction is not provided it
     * will save the file in the current directory with the name "received_" + the
     * name of the file requested.
     */
    private static String saveLocation = "received_";

    /**
     * The buffer to hold the data from the server. This is the buffer that holds
     * the data from the server to be written to the file. this is the previous data
     * packet
     * that was recieved. This is used to check if the block number is the same as
     * the previous
     * block number. If the block number is the same the data is not written to the
     * file as it is a duplicate.
     * else the data is written to the file.
     */
    private static byte[] repsonseBuffer;

    /**
     * This is the main method of the TftpClient program. It takes the server
     * address, port, file path and save location as arguments. It then creates a
     * socket to the server and sends a request for the file. It then waits for a
     * response from the server and handles the response. If the response is an
     * error packet it prints the error message to the console. If the response is
     * the end of the file it exits the program. If the response is a data packet it
     * holds the data in a buffer until the next data packet is recieved and checks
     * if the 2 packets are differnt blocks
     * if they are differnt it writes the first to the file. It then sends an ACK
     * packet to the server for the block it received. If the
     * server does not respond within 30 seconds it closes the connection.
     * 
     * The arguments should be in the format server:port/filePath save
     * location
     * 
     * @param args the arguments passed to the program
     */
    public static void main(String[] args) {
        try {
            // check for correct number of arguments
            // if (args.length > 2 || args.length < 1) {
            // System.err.println("Usage: java TftpClient <server>:<port>/<filePath>
            // <savelocation>");
            // System.exit(1);
            // }
            args = new String[] { "localhost:69/test" };

            // parse arguments given
            ParseArgs(args);

            // create socket to file requsting server
            ds = new DatagramSocket();

            // create request message
            byte[] data = filename.getBytes();
            byte type = 1;
            byte[] message = new byte[data.length + 1];
            message[0] = type;
            System.arraycopy(data, 0, message, 1, data.length);

            // send request
            DatagramPacket packet = new DatagramPacket(message, 0, message.length, serverAddress, port);
            ds.send(packet);

            // prepare block numbers for later checking
            byte responseBlockNumber = 0;
            byte prevBlockNumber = -1;

            // get response
            for (;;) {
                // set up beffer, packet and set timeout
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.setSoTimeout(30000);

                // get response from server, if server does not respond in 30 seconds close the
                // conection and exit
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

                // make TftpPacket from response
                TftpPacket handeledPacket = new TftpPacket(p);

                // handle response
                responseBlockNumber = HandleResponse(handeledPacket);

                // check for duplicate data packet
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

            }

        } catch (Exception e) {
            // print out any exceptions
            System.err.println("Exception: " + e.getMessage());

        }

    }

    /**
     * This method parses the arguments passed to the program. It checks for the
     * correct number of arguments and then parses the server address, port, file
     * path and save location. It then prints the server address, port, file path
     * and save location to the console.
     * 
     * The first argument should be in the format server:port/filePath and the
     * second argument should be the save location.
     * 
     * If the second argument is not provided the file will be saved in the current
     * directory with "recived_" + the name of the file requested.
     * 
     * Once the arguments have been parsed the server address is resolved and stored
     * in the serverAddress variable. If the server adress cant be resolved it will
     * throw an
     * UnknownHostException.
     * 
     * </br>
     * </br>
     * <dt>Default values:</dt>
     * <ul>
     * <li>server: loaclhost</li>
     * <li>port: 69</li>
     * <li>save location: received_ + fileRequested</li>
     * </ul>
     * 
     * @param args the arguments passed to the program
     * @throws UnknownHostException if the server address can not be resolved
     */
    private static void ParseArgs(String[] args) throws UnknownHostException {

        // get where the file path starts as an index
        int indexOfStartOfFilePath = args[0].indexOf("/");
        if (indexOfStartOfFilePath < 0) {
            System.out.println("Invalid input");
            return;
        }

        // get serverAndPort and filePath as 2 strings
        String filePath = args[0].substring(indexOfStartOfFilePath);
        String serverAndPort = args[0].substring(0, indexOfStartOfFilePath);

        // split the serverAndPort string into server and port
        String[] serverAndPortSplit = serverAndPort.split(":");
        // set server and port
        String server = serverAndPortSplit.length != 1 ? serverAndPortSplit[0] : "localhost";
        port = Integer.parseInt(serverAndPortSplit.length > 1 ? serverAndPortSplit[1].split("/")[0] : "69");

        // get file name and dir (fileName has to be after the last /)
        int fileNameStartIndex = filePath.lastIndexOf("/") + 1;
        // set filename and dir
        String dir = fileNameStartIndex > 1 ? filePath.substring(1, fileNameStartIndex) : "";
        filename = fileNameStartIndex > 0 ? filePath.substring(fileNameStartIndex) : "";
        filename = dir + filename;

        // set server address from server string
        serverAddress = InetAddress.getByName(server);

        // set save location
        if (args.length == 2) {
            saveLocation = args[1];
        } else {
            // if save location is not provided save in current directory with received_ +
            // fileName
            saveLocation += filename;
        }

        // tell user all set values (tell client the context for the program)
        System.out.println("Requesting file: " + filename);
        System.out.println("Requesting file path: " + dir);
        System.out.println("Save location: " + saveLocation);
        System.out.println("Server: " + server);
        System.out.println("Port: " + port);
    }

    /**
     * This method handles a {@link TftpPacket TftpPacket} and returns the block
     * number
     * that needs to be acknowledged.
     * It checks for error packets and prints the error message to the console. It
     * also checks for the end of the file
     * and exits the program if all blocks have been received. The data from the
     * packet is stored in the response buffer
     * to be written to the file.
     * 
     * @param p a TftpPacket to be handled
     * @return the block number of the response sent from the server
     * @see TftpPacket
     */
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
        if (p.getData().length < 512 || p.getData().length == 0) {
            System.out.println("All blocks received");
            WriteToFile(p.getData());
            System.exit(0);
        }

        byte responseBlockNumber = p.getBlockNumber();
        repsonseBuffer = p.getData();

        // return with the block number to acknowledge
        return responseBlockNumber;
    }

    /**
     * This method sends an ACK packet to the server. It uses the
     * {@link #Respond(DatagramPacket) Respond} method to
     * send the Acknowledgement. This takes a packet and adds the ACK type to the
     * start of the packet. the block numbers
     * needs to be added to the packet before calling this method in the first spot.
     * any exceptions are caught and printed to the console.
     * 
     * @param p the packet with the block number to be sent to the server
     * @see #Respond(DatagramPacket)
     * @see DatagramPacket
     */
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

    /**
     * This method sends a DatagramPacket to the server. the packet is the response
     * to give.
     * This is a helper method that can send a response at anytime with a given
     * packet. any
     * exceptions are caught and printed to the console.
     * 
     * @param p the packet to be sent to the server\
     * @see DatagramPacket
     */
    private static void Respond(DatagramPacket p) {
        try {
            ds.send(p);
        } catch (Exception e) {
            System.err.println("Error sending response");
        }
    }

    /**
     * This method writes an array of bytes to the file specified by
     * {@link saveLocation saveLocation}.
     * it uses a FileOutputStream to write the data to the file with append
     * specified
     * as true, so that the data is appended to the end of the file.
     * 
     * @param data the array of bytes to be written to the file
     * @see FileOutputStream
     * @see saveLocation
     */
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