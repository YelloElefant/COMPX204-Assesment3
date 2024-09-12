import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class test {

    public static void main(String[] args) throws InterruptedException {
        OutPutStream.out("Hello, World!");
        OutPutStream.out("Hello, World!");
        OutPutStream.out("Hello, World!");
        OutPutStream.out("Hello, World!");
        Thread.sleep(5000);
        OutPutStream.clear();
    }

}