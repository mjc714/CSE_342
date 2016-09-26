
import java.net.*;
import java.util.Random;

/**
 * UDPEchoGateway to store and forward packets from client --> server can be
 * configured to drop % packets
 *
 * @author Matthew
 */
public class UDPEchoGateway {

    public static void main(String[] args) {

        DatagramSocket sock;

        int gatewayPort;
        String serverIP;

        int serverPort;
        InetAddress serverIPAddress;

        int clientPort = 0;
        InetAddress clientIPAddress = null;

        DatagramPacket pack = new DatagramPacket(new byte[20], 20);

        Random rand = new Random();
        int dropChance = 0; //percent of packets to drop
        int usrDropChance = 0;

        if (args.length != 4) {
            System.out.println("args.length is " + args.length + "\n");
            System.out.println("Usage: java UDPEchoGateway <gateway-port: 12345> <server-ip: x.x.x.x> <server-port: 12345> <drop-chance(0-100)> 10\n");
            System.exit(1);
        }

        try {
            InetAddress gatewayIP = InetAddress.getLocalHost();
            System.out.println("Gateway IP is " + gatewayIP.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println(e);
        }

        gatewayPort = (new Integer(args[0]));
        System.out.println("gateway port is " + gatewayPort + "\n");

        serverIP = args[1];
        serverPort = (new Integer(args[2]));
        try {
            sock = new DatagramSocket(gatewayPort);
        } catch (SocketException e) {
            System.out.println(e);
            return;
        }

        //convert string arg into double value
        usrDropChance = Integer.valueOf(args[3]);

        while (true) {
            dropChance = rand.nextInt(100) + 1;
            //System.out.println("Drop chance: " + dropChance + "\n");
            try {
                serverIPAddress = InetAddress.getByName(serverIP);
                sock.receive(pack);
                System.out.println("Receiving from " + pack.getAddress() + "\n");
                if (!pack.getAddress().equals(serverIPAddress)) {
                    System.out.println("Receiving from client\n");
                    clientIPAddress = pack.getAddress();
                    clientPort = pack.getPort();
                    if (dropChance < usrDropChance) { //check userdrop rate against randomly generated drop rate
                        pack = new DatagramPacket(pack.getData(), pack.getData().length, serverIPAddress, serverPort);
                        sock.send(pack);
                        System.out.println("Packet sent to server: " + serverIPAddress + "\n");
                    }
                } else if (pack.getAddress().equals(serverIPAddress) && clientPort != 0) {
                    System.out.println("Receiving from server\n");
                    if (dropChance < usrDropChance) { //check userdrop rate against randomly generated drop rate
                        pack = new DatagramPacket(pack.getData(), pack.getData().length, clientIPAddress, clientPort);
                        sock.send(pack);
                        System.out.println("Packet sent to client: " + clientIPAddress + "\n");
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
}
