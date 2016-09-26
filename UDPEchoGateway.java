/*
 * Matthew Chin
 * mjc714
 * CSE 342 Lab 2 UDP Echo with gateway
 */
import java.io.*; 
import java.net.*;
import java.util.Random;

public class UDPEchoGateway {
    public static void main(String[] args) {
	int BUFFERSIZE = 256;
	DatagramSocket sock;
	
	int gatewayPort;
	String serverIP;
	
	int serverPort;
	InetAddress serverIPAddress;
	
	int clientPort = 0;
	InetAddress clientIPAddress = null;
	
	//DatagramPacket pack = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
	DatagramPacket pack = new DatagramPacket(new byte[20], 20);
	
	Random rand = new Random();
	double dropChance = 0; //percent of packets to drop
	
	if(args.length != 3) {
	    System.out.println("args.length is " + args.length + "\n");
	    System.out.println("Usage: java UDPEchoGateway 5678 <server-ip: x.x.x.x> <server-port: 12345>\n");
	    System.exit(1);
	}
	
	try{
	    InetAddress gatewayIP = InetAddress.getLocalHost();
	    System.out.println("Gateway IP is " + gatewayIP.getHostAddress());
	}catch(UnknownHostException e){
	    System.out.println(e);
	}
	
	gatewayPort = (new Integer(args[0])).intValue();
	System.out.println("gateway port is " + gatewayPort + "\n");
	serverIP = args[1];
	serverPort = (new Integer(args[2])).intValue();
	try {
	    sock = new DatagramSocket(gatewayPort);
	} catch (SocketException e) {
	    System.out.println(e);
	    return;
	}
	// relay everything
	while (true) {
	    //dropChance = rand.nextInt(10) + 1;
	    //System.out.println("Drop chance: " + dropChance + "\n");
	    try {
		serverIPAddress = InetAddress.getByName(serverIP);
		sock.receive(pack);
		System.out.println("Receiving from " + pack.getAddress() + "\n");
		if(!pack.getAddress().equals(serverIPAddress)) {
		    System.out.println("Receiving from client\n");
		    clientIPAddress = pack.getAddress();
		    clientPort = pack.getPort();
		    //if{//dropChance > 3){ //roll > 3 and the gateway forwards the packet to the Server
		    pack = new DatagramPacket(pack.getData(), pack.getData().length, serverIPAddress, serverPort);
		    sock.send(pack);
		    System.out.println("Packet sent to server: " + serverIPAddress + "\n");
		    //}
		}
		else if(pack.getAddress().equals(serverIPAddress) && clientPort != 0) {
		    System.out.println("Receiving from server\n");
		    //if{//dropChance > 3){ //roll > 3 and the gateway forwards the packet to the Client
		    pack = new DatagramPacket(pack.getData(), pack.getData().length, clientIPAddress, clientPort);           
		    sock.send(pack);
		    System.out.println("Packet sent to client: " + clientIPAddress + "\n");
		    //}
		}
	    } catch (Exception ex) {
		System.out.println(ex);
	    }
	}
    }
}