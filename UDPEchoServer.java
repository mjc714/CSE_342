
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

        DatagramSocket serverSocket = null;

        DatagramPacket pack = new DatagramPacket(new byte[20], 20);
        DatagramPacket ack;

        byte[] rcvdBuffer = new byte[51080];

        FileOutputStream fos = null;

        byte seqChk = 0;

        int count = 0;
        int serverPort = 0;

        boolean done = false;

        if (args.length != 2) {
            System.out.println("args.length is " + args.length + "\n");
            System.out.println("Usage:  %s <UDP SERVER PORT> <Window Size>\n");
            System.exit(1);
        }

        serverPort = (new Integer(args[0]));
        System.out.println("server port is " + serverPort + "\n");

        /**
         * Get the server IP to display to user for connecting to Gateway
         */
        try {
            InetAddress serverIPAddress = InetAddress.getLocalHost();
            System.out.println("Server IP is " + serverIPAddress.getHostAddress() + "\n");
        } catch (UnknownHostException e) {
            System.out.println(e);
        }

        /**
         * Create a server socket
         */
        try {
            serverSocket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            System.out.println(e);
            return;
        }

        /**
         * open file output stream to write to specified file
         */
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
                if (count != 2688) { //we do not have the last datagram

                    /**
                     * we found a repeat seqNum, send an ACK back to client
                     * saying we already got this; this is needed in the event
                     * the gateway drops our original ACK; skip to the next
                     * iteration, not incrementing count
                     */
                    if (pack.getData()[0] != seqChk) {
                        ack = new DatagramPacket(pack.getData(), pack.getData().length,
                                pack.getAddress(), pack.getPort());
                        serverSocket.send(ack);
                        continue;
                    }

                    /**
                     * loop through 19 bytes excluding the seqChk, multiply 19
                     * by the seqChk to get the last place we buffered in data
                     * then add an offset of k to read ahead
                     */
                    for (int k = 0; k < 19; k++) {
                        rcvdBuffer[count * 19 + k] = pack.getData()[k + 1];
                    }
                    /**
                     * here we have successfully received a datagram packet send
                     * a datagram back to client
                     */
                    ack = new DatagramPacket(pack.getData(), pack.getData().length, pack.getAddress(), pack.getPort());
                    serverSocket.send(ack);

                    //update seqChk for the next datagram  
                    if (seqChk == 0) {
                        seqChk = 1;
                    } else {
                        seqChk = 0;
                    }
                } else { //do we have the last datagram?
                    //we're at the last datagram so read from 1 + the last index until the end of the file
                    for (int n = count * 19; n < 51080; n++) {
                        //read into the buffer the nth element from the end
                        rcvdBuffer[n] = pack.getData()[n - count * 19 + 1];
                    }
                    ack = new DatagramPacket(pack.getData(), pack.getData().length, pack.getAddress(), pack.getPort());
                    serverSocket.send(ack);
                }
                count++;
            }
        } catch (SocketException ex) {
            System.out.println(ex + "\nWriting buffer to file now.\n");
        } catch (IOException ex) {
            System.out.println(ex + "\nWriting buffer to file now.\n");
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
