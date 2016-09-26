
import java.io.*;
import java.net.*;

/**
 * UDPEchoServer that receives a n datagram packets from the gateway which
 * received it from a client, server appends datagrams and writes to new file
 *
 * @author Matthew
 */
public class UDPEchoServer {

    public static void main(String[] args) {

        DatagramSocket sock;
        DatagramPacket pack = new DatagramPacket(new byte[20], 20);
        DatagramPacket ack;

        byte[] ackMsg = new byte[1];

        FileOutputStream fos = null;

        //buffer to store received bytes
        //479 used as testing, implement robustness
        int seqNum = 0;
        int indexTrack = 0;
        int serverPort = 0;

        boolean done = false;

        if (args.length != 1) {
            System.out.println("args.length is " + args.length + "\n");
            System.out.println("Usage:  %s <UDP SERVER PORT>, <UDP SERVER IP>\n");
            System.exit(1);
        }

        serverPort = (new Integer(args[0]));
        System.out.println("server port is " + serverPort + "\n");
        try {
            InetAddress serverIPAddress = InetAddress.getLocalHost();
            System.out.println("Server IP is " + serverIPAddress.getHostAddress() + "\n");
        } catch (UnknownHostException e) {
            System.out.println(e);
        }

        try {
            sock = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            System.out.println(e);
            return;
        }

        try {
            fos = new FileOutputStream("CSE342Recvd.txt", true);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            while (true) {

                sock.setSoTimeout(5000);
                sock.receive(pack);

                System.out.println("Received packet " + seqNum + " from " + pack.getAddress());
                //send an ack message back to client
                //ack = new DatagramPacket(ackMsg, ackMsg.length, pack.getAddress(), pack.getPort());
                //sock.send(ack);

                for (int i = 1; i < 20; i++) {
                    fos.write(pack.getData());
                }
            }
        } catch (SocketException ex) {
            try {
                fos.close();
            } catch (IOException ioex) {
                System.out.println(ioex);
            }
            sock.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
