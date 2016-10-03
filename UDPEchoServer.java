
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * UDPEchoServer that receives a n datagram packets from the gateway which
 * received it from a client, server appends datagrams and writes to new file
 *
 * @author Matthew - mjc714
 */
public class UDPEchoServer {

    public static void main(String[] args) {

        DatagramSocket serverSocket = null;

        DatagramPacket pack = new DatagramPacket(new byte[20], 20);
        DatagramPacket ack;

        byte[] ackMsg = new byte[1];
        byte[] rcvdBuffer = new byte[51080];
        ArrayList<Integer> dupCheck = new ArrayList<>(26);

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
            serverSocket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            System.out.println(e);
            return;
        }

        try {
            //fos = new FileOutputStream("CSE342Recvd.txt", false);
            fos = new FileOutputStream("asciiRcvd.gif");
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            serverSocket.setSoTimeout(30000);
            while (true) {
                serverSocket.receive(pack); //get the packet from the Gateway
                if (pack.getData()[0] != 2689) { //we do not have the last datagram
                    for (int c : dupCheck) {
                        //we found a duplicate seqNum
                        if (pack.getData()[0] == dupCheck.get(c)) {
                            //send an ACK back to client saying we already got this
                            //this is needed in the event the gateway drops our original ACK
                            ack = new DatagramPacket(pack.getData(), pack.getData().length, pack.getAddress(), pack.getPort());
                            serverSocket.send(ack);
                        } else { //no duplicates found carry with normal routine
                            //add new seqNum to duplicate check
                            dupCheck.add((int) pack.getData()[0]);
                        }
                    }
                    for (int k = 0; k < 19; k++) { //loop through 19 bytes excluding the seqNum
                        //mult 19 by the seqNum to get the last place we buffered in data
                        //then add an offset of k to read ahead
                        rcvdBuffer[pack.getData()[0] * 19 + k] = pack.getData()[k + 1];
                    }

                    //here we have successfully received a datagram packet
                    //send a datagram back to client
                    ack = new DatagramPacket(pack.getData(), pack.getData().length, pack.getAddress(), pack.getPort());
                    serverSocket.send(ack);

                } else { //do we have the last datagram?
                    //we're at the last datagram so read from 1 + the last index until the end of the file
                    for (int n = pack.getData()[0] * 19; n < 51080; n++) {
                        //read into the buffer the nth element from the end
                        rcvdBuffer[n] = pack.getData()[n - pack.getData()[0] * 19 + 1];
                    }
                    ack = new DatagramPacket(pack.getData(), pack.getData().length, pack.getAddress(), pack.getPort());
                    serverSocket.send(ack);
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex + "\nWriting buffer to file now.\n");
        } catch (IOException ex) {
            System.out.println(ex);
        } finally { //after we receive all 26 datagrams, write to file and close the filestream
            try {
                fos.write(rcvdBuffer);
                fos.close();
                serverSocket.close();
            } catch (IOException iox) {
                System.out.println(iox);
            }
        }
    }
}
