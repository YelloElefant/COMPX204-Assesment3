/**
 * InvalidPacketException is a custom exception class that is thrown when a
 * packet is invalid.
 * This exception is thrown when the packet type is invalid, the packet does not
 * contain a block number,
 * or the packet does not contain data.
 * 
 * @see TftpPacket
 * @see TftpWorker
 * @see TftpServer
 * @see TftpClient
 * 
 */
public class InvalidPacketException extends Exception {

    /**
     * Constructs a new InvalidPacketException with the specified detail message.
     * param message to the base class constructor
     * 
     * @param message the detail message
     */
    public InvalidPacketException(String message) {
        super(message);
    }
}