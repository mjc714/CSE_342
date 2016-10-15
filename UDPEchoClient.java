
import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
        int ackCount = 0;
        int temp = 0;
        int fileSize = 0;

        //go back n vars
        int sendPackNum = 0;
        int winSize = 0;
        int pcktNum = 0;

        boolean done = false;
        boolean ack = false;

        DatagramPacket sendDatagramPacket;
        DatagramPacket rcvdDatagramPacket = new DatagramPacket(new byte[20], 20);

        DatagramSocket clientSocket = null;

        byte sendBuffer[] = new byte[20];

        //sequence number of sent UDP datagram, 0|1, alternating depending on ACK receipt
        byte seqNum = 0;

        if ((args.length != 3)) {
            System.out.println("Usage: %s <Gateway IP> <Gateway Port> <Sliding Window Size>\n");
            System.exit(1);
        }

        gatewayIP = args[0];
        gatewayPort = (new Integer(args[1]));

        //init the window
        winSize = Integer.parseInt(args[2]);

        /**
         * this will hold the packets that we have sent that correspond to the
         * window size, we then store those in here waiting for an ACK when we
         * receive an ACK we can remove the corresponding packet from the
         * ArrayList
         */
        ArrayList<DatagramPacket> winBuffer = new ArrayList<>();
        ArrayList<DatagramPacket> remBuffer = new ArrayList<>();
        try {
            clientSocket = new DatagramSocket();
            gatewayIPAddress = InetAddress.getByName(gatewayIP);

            try {

                //read file bytes into array
                //FileInputStream inputStream = new FileInputStream("CSE342.txt");
                FileInputStream inputStream = new FileInputStream("ascii.gif");
                while ((temp = inputStream.read()) != -1) {
                    fileSize++;
                }
                inputStream.close();
                byte buffer[] = new byte[fileSize];

                //inputStream = new FileInputStream("CSE342.txt");
                inputStream = new FileInputStream("ascii.gif");
                while ((temp = inputStream.read(buffer)) != -1) {
                }
                inputStream.close();

                //count should be 2689 for ascii.gif
                count = (fileSize / 19) + ((fileSize % 19 == 0) ? 0 : 1);

                /**
                 * send out winSize packets to the server after each send,
                 * calculate the frameNum and add it to winBuffer of size
                 * winSize; keep sending these n packets; when we receive ACKS
                 * we send the next p packets that haven't been worked on yet
                 * with p being the number of packets we have ACKs for
                 */
                boolean flag = false;
                int t = 0;
                while (ackCount != (count - 1)) {
                    for (t = 0; t < winSize; t++) {
                        //pcktNum is replacing this for loop count
                        if (seqNum == 0) {
                            sendBuffer[0] = seqNum++;
                        } else {
                            sendBuffer[0] = seqNum--;
                        }
                        for (int j = 1; j < 20; j++) {
                            //we have not gone through the first round
                            //of the window, 0 -> winSize
                            if (!flag) {
                                sendBuffer[j] = buffer[t * 19 + j - 1];
                            } else {
                                /**
                                 * we have gone through the first round now we
                                 * must start accumulating iteration counts into
                                 * pcktNum
                                 *
                                 * working with pcktNum the first time we need
                                 * to modify it to winSize anytime afterwards we
                                 * increment pcktNum which should get the
                                 * correct packet from buffer
                                 */

                                //go back
                                if (ackCount < pcktNum) {
                                    pcktNum = ackCount + 1;
                                }

                                if (pcktNum == winSize) {
                                    sendBuffer[j] = buffer[pcktNum * 19 + j - 1];
                                    pcktNum += 1;
                                } else {
                                    //System.out.println("pcktNum: " + pcktNum);
                                    sendBuffer[j] = buffer[pcktNum * 19 + j - 1];
                                    pcktNum += 1;
                                }
                            }
                        }

                        /**
                         * check if winBuffer is empty: if it is then move ahead
                         * since this is the first time running through
                         * otherwise this is not our first time and we have
                         * packets that we did not receive an ACK for, so resend
                         * them here along with any additional packets up to the
                         * winSize
                         */
                        if (!(winBuffer.isEmpty())) {
                            for (DatagramPacket dp : winBuffer) {
                                clientSocket.send(dp);
                            }
                        }

                        //send winSize packets over to the server
                        sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, gatewayIPAddress, gatewayPort);
                        clientSocket.send(sendDatagramPacket);

                        //keep actual count of first set of iterations
                        if (!flag) {
                            pcktNum++;
                        }
                        /**
                         * put the packet into the winBuffer this will grow the
                         * window buffer with unACK'ed packets **WE NEED TO HOLD
                         * THIS TO THE ACTUAL WINDOW SIZE!**
                         */
                        winBuffer.add(sendDatagramPacket);
                    }
                    /**
                     * update flag if we have gone through the initial set of
                     * winSize iterations, after the first iteration this should
                     * never run again, because flag will always be false
                     */
                    if ((pcktNum == (winSize)) && !flag) {
                        flag = true;
                    }

                    /**
                     * we sent winSize packets to the server, now we handle ACKs
                     * loop through winBuffer and check if we have received ACKS
                     * for these packets if so remove them; try this a few
                     * times?
                     */
                    clientSocket.setSoTimeout(50);
                    for (DatagramPacket c : winBuffer) {
                        //clientSocket.setSoTimeout(50);
                        try {
                            clientSocket.receive(rcvdDatagramPacket);
                            if (seqNum == 0) {
                                if (rcvdDatagramPacket.getData()[0] != seqNum) {
                                    System.out.println("Received ACK: " + ackCount);
                                    ackCount++;
                                    remBuffer.add(c);
                                }
                            } else {
                                if (rcvdDatagramPacket.getData()[0] != seqNum) {
                                    System.out.println("Received ACK: " + ackCount);
                                    ackCount++;
                                    remBuffer.add(c);
                                }
                            }
                        } catch (SocketTimeoutException soex) {
                        }
                    }
                    /**
                     * clear out the remove buffer of ACK'ed packets
                     */
                    remBuffer.clear();
                    //we have sent all 2688 packets; break
                }

                //update the seqChk
                if (seqNum == 0) {
                    sendBuffer[0] = seqNum++;
                } else {//make seqNum = 1
                    sendBuffer[0] = seqNum--;
                }

                //clear out old send buffer
                sendBuffer = new byte[20];
                sendBuffer[0] = 0;

                //put last few uneven bytes into sendBuffer
                for (int k = (count - 1) * 19; k < fileSize; k++) {
                    sendBuffer[k - (count - 1) * 19 + 1] = buffer[k];
                }
                //send last packet to gateway to be forwarded to server
                sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, gatewayIPAddress, gatewayPort);
                clientSocket.send(sendDatagramPacket);

                //wait to see if the server has received the last packet
                while (!ack) {
                    clientSocket.setSoTimeout(50);
                    try {
                        clientSocket.receive(rcvdDatagramPacket);
                        if (rcvdDatagramPacket.getData()[0] == (seqNum)) {
                            ack = true;
                            //System.out.println("Received ACK: " + (ackCount + 1));
                        }
                    } catch (SocketTimeoutException ex) {
                        clientSocket.send(sendDatagramPacket);
                    }
                }
            } catch (FileNotFoundException exf) {
                System.out.println(exf);
            } catch (IOException ioex) {
                System.out.println(ioex);
            }
        } catch (SocketException | UnknownHostException ex) {
            System.out.println("Socket Error: " + ex);
        } finally {
            clientSocket.close();
        }
    }
}
