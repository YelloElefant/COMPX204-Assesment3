import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class test {

    public static void main(String[] args) {
        try {
            File file = new File("a3.pdf");
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = fis.readAllBytes();

            GetBlocks(fileData);

        } catch (Exception e) {

        }
    }

    // List<byte[]>
    private static void GetBlocks(byte[] data) {
        List<byte[]> blocks = new ArrayList<byte[]>();

        int amountOfBlocks = data.length / 512;

        int start = 0;
        int end = 512;
        byte[] block;

        while (end / 512 != amountOfBlocks + 1) {
            block = Arrays.copyOfRange(data, start, end);
            blocks.add(block);
            start += 512;
            end += 512;

        }
        block = Arrays.copyOfRange(data, start, data.length);
        blocks.add(block);

        byte[] backWards = joinArrays(blocks);

        System.out.println(blocks);
    }

    private static byte[] joinArrays(List<byte[]> blocks) {
        int totalLength = 0;
        for (byte[] block : blocks) {
            totalLength += block.length;
        }

        byte[] result = new byte[totalLength];
        int currentIndex = 0;

        for (byte[] block : blocks) {
            System.arraycopy(block, 0, result, currentIndex, block.length);
            currentIndex += block.length;
        }

        return result;

    }

    private static byte[] MakePacket(byte type, byte[] data) {
        byte[] packet = new byte[1 + data.length];
        packet[0] = type;
        for (int i = 1; i < packet.length; i++) {
            packet[i] = data[i - 1];
        }
        return packet;
    }
}