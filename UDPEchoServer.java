import java.io.*;
import java.nio.file.*;
import java.net.*; 

public class UDPEchoServer {
    public static void main(String[] args) {
	int BUFFERSIZE = 256;
	DatagramSocket sock;
	
	int serverPort;
	//DatagramPacket pack = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
	DatagramPacket pack = new DatagramPacket(new byte[20], 20);
	
	DatagramPacket ack;
	
	byte[] ackMsg = new byte[1];
	
	//Path file = Paths.get("CSE342Recvd.txt");
	FileOutputStream fos = null;
	
	//buffer to store received bytes
	//479 used as testing, implement robustness
	byte[][] recvdBytes = new byte[24][19];
	int seqNum = 0;
	int indexTrack = 0;
	
	/* seperate counter for recvdBytes to rememeber where it left off reading
	   bytes from the socket
	*/
	int temp = 0;
	
	boolean done = false;
	
	if(args.length != 1) {
	    System.out.println("args.length is " + args.length + "\n");
	    System.out.println("Usage:  %s <UDP SERVER PORT>, <UDP SERVER IP>\n");
	    System.exit(1);
	}
	
	serverPort = (new Integer(args[0])).intValue();
	System.out.println("server port is " + serverPort + "\n");
	try{
	    InetAddress serverIPAddress = InetAddress.getLocalHost();
	    System.out.println("Server IP is " + serverIPAddress.getHostAddress() + "\n");
	}catch(UnknownHostException e){
	    System.out.println(e);
	}
	
	try {
	    sock = new DatagramSocket(serverPort);
	} catch (SocketException e) {
	    System.out.println(e);
	    return;
	}
	
	try{
	    fos = new FileOutputStream("CSE342Recvd.txt", true);
	}
	catch(FileNotFoundException ex){
	    System.out.println(ex);
	}
	
	try{
	    while (!done) {
		/*
		  this should get an array of byte data from pack
		  we then store pack.getData()[0] in trackIndex, which should be the seqNum,
		  into the corresponding index in recvdBytes for ordering
		  recvdBytes[trackIndex][i-1], thus ensuring the
		  correct assembly order of the file.
		*/
		
		sock.receive(pack);
		System.out.println("Received packet " + seqNum + " from " + pack.getAddress());
		//send an ack message back to client
		ack = new DatagramPacket(ackMsg, ackMsg.length, pack.getAddress(), pack.getPort());
		sock.send(ack);
		
		for(int i = 1; i < 20; i++){
		    recvdBytes[seqNum][i-1] = pack.getData()[i];
		}
		seqNum++;
		if(seqNum == 24){
		    done = true;
		}
	    }
	    
	    seqNum = 0;
	    
	    //write rcvdBytes to file
	    while(seqNum < 24){
		fos.write(recvdBytes[seqNum]);
	    }
	    
	    //close file output stream
	    fos.close();
	}catch(IOException ex){
	    System.out.println(ex);
	}
	//close server socket
	sock.close();
    }
}