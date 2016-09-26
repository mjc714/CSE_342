
import java.io.*;
import java.net.*;

/**
 * UDPEchoServer that receives a n datagram packets from the gateway which
 * received it from a client, server appends datagrams and writes to new file
 *
 * @author Matthew - mjc714
 */
public class UDPEchoServer {

    public static void main(String[] args) {

        DatagramSocket sock;
        DatagramPacket pack = new DatagramPacket(new byte[20], 20);
        DatagramPacket ack;

        byte[] ackMsg = new byte[1];
        byte[] rcvdBuffer = new byte[479];

        FileOutputStream fos = null;

        //buffer to store received bytes
        //479 used as testing, implement robustness
        int seqNum = 0;
        int length = 0;
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
            fos = new FileOutputStream("CSE342Recvd.txt", false);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            while (true) {
                sock.receive(pack); //get the packet from the Gateway
                if (pack.getData()[0] != 25) { //we do not have the last datagram
                    for (int k = 0; k < 19; k++) { //loop through 19 bytes excluding the seqNum
                        //mult 19 by the seqNum to get the last place we buffered in data
                        //then add an offset of k to read ahead
                        rcvdBuffer[pack.getData()[0] * 19 + k] = pack.getData()[k + 1];
                    }
                } else { //have we received the last datagram?
                    //we're at the last datagram so read from 1 + the last index until the end of the file
                    for (int n = pack.getData()[0] * 19; n < 479; n++) {
                        //read into the buffer the nth element from the end
                        rcvdBuffer[n] = pack.getData()[n - pack.getData()[0] * 19 + 1];
                    }
                    break;
                }
                //send an ack message back to client
                //ack = new DatagramPacket(ackMsg, ackMsg.length, pack.getAddress(), pack.getPort());
                //sock.send(ack);
            }

            //write to "CSE342Rcvd.txt"
            fos.write(rcvdBuffer);

        } catch (SocketException ex) {
            try {
                fos.close();
            } catch (IOException ioex) {
                System.out.println(ioex);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
        sock.close();
    }
}
