
import java.io.*;
import java.net.*;

/**
 * UDPEchoClient Class reads in a file, splits it into n datagrams each of 20
 * bytes where the first byte is a seqNum
 *
 * @author Matthew - mjc714
 */
public class UDPEchoClient {

    public static void main(String args[]) {

        String gatewayIP;
        InetAddress gatewayIPAddress;

        int gatewayPort;
        int count = 0;
        int temp = 0;
        int fileSize = 0;

        boolean done = false;

        DatagramPacket sendDatagramPacket, rcvdDatagramPacket;
        DatagramSocket clientSocket;

        byte sendBuffer[] = new byte[20];

        //sequence number of sent UDP datagram, 0-19, 20 datagrams should be sent
        byte seqNum = 0;

        if ((args.length != 2)) {
            System.out.println("Usage: %s <Gateway IP> [<Gateway Port>]\n");
            System.exit(1);
        }

        gatewayIP = args[0];

        if (args.length == 2) {
            gatewayPort = (new Integer(args[1]));
        } else {
            gatewayPort = 7;
        }

        try {
            clientSocket = new DatagramSocket();
            gatewayIPAddress = InetAddress.getByName(gatewayIP);

            try {

                //read file bytes into array
                FileInputStream inputStream = new FileInputStream("CSE342.txt");
                while ((temp = inputStream.read()) != -1) {
                    fileSize++;
                }
                inputStream.close();

                byte buffer[] = new byte[fileSize];

                inputStream = new FileInputStream("CSE342.txt");
                while ((temp = inputStream.read(buffer)) != -1) {
                }
                inputStream.close();

                count = (fileSize / 19) + ((fileSize % 19 == 0) ? 0 : 1);

                //fill sendBuffer
                for (int i = 0; i < (count - 1); i++) {
                    sendBuffer[0] = seqNum++;
                    for (int j = 1; j < 20; j++) {
                        sendBuffer[j] = buffer[i * 19 + j - 1];
                    }
                    //send first set of datagrams to gateway
                    sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, gatewayIPAddress, gatewayPort);
                    clientSocket.send(sendDatagramPacket);
                }

                //clear out old send buffer
                sendBuffer = new byte[20];
                sendBuffer[0] = seqNum;
                //put last few uneven bytes into sendBuffer
                for (int k = (count - 1) * 19; k < fileSize; k++) {
                    sendBuffer[k - (count - 1) * 19 + 1] = buffer[k];
                }
                //send last packet to gateway to be forwarded to server
                sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, gatewayIPAddress, gatewayPort);
                clientSocket.send(sendDatagramPacket);

                /*
                 take payload array and execute stop/wait ARQ
                 save the current seqNum, then we send a datagram to the server
                 stop and wait for an ACK from the server, in this case we will use the seqNum
                 if the serverSeqNum == the client's seqNum, we send the next datagram
                 */ //create/send the first datgram to the server, then enter into while that will do stop/wait
                //{
                //    sendDatagramPacket = new DatagramPacket(payLoadArray[0], payLoadArray[0].length, serverIPAddress, serverPort);
                // }
                // clientSocket.send(sendDatagramPacket);
                //seqNum = 0;
            /*
                 while (!done) {

                 //wait for/get return packet from server
                 clientSocket.setSoTimeout(5000);

                 try {
                 rcvdDatagramPacket = new DatagramPacket(rcvData, rcvData.length);
                 clientSocket.receive(rcvdDatagramPacket);

                 //received ACK, send the next packet, seqNum ++;
                 seqNum++;

                 //create next datagram to send
                 sendDatagramPacket = new DatagramPacket(payLoadArray[seqNum], payLoadArray[seqNum].length, serverIPAddress, serverPort);
                 clientSocket.send(sendDatagramPacket);
                 //we have gone through all the datagrams, DONE!
                 if (seqNum == 23) {
                 done = true;
                 }
                 */
//            } catch (SocketTimeoutException ex) {
//                clientSocket.send(sendDatagramPacket);
            } catch (FileNotFoundException exf) {
                System.out.println(exf);
            } catch (IOException ioex) {
                System.out.println(ioex);
            }
            //}//while

            //close client socket
            clientSocket.close();
        } catch (SocketException | UnknownHostException ex) {
            System.out.println("Socket Error: " + ex);
        }
    }
}
