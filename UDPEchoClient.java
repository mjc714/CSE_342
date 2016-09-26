import java.io.*;
import java.nio.file.Files;
import java.net.*;
import java.util.*;

public class UDPEchoClient {
    public static void main(String args[])
    {
	//int ECHOMAX = 256;
	//byte[] receiveData = new byte[ECHOMAX];
	
	byte[] rcvData = new byte[256];
	
	String serverIP;
	InetAddress serverIPAddress;
	
	int serverPort;
	String echoString;
	
	int count = 0;
	
	boolean done = false;
	
	String receivedString;
	DatagramPacket sendDatagramPacket, rcvdDatagramPacket;
	DatagramSocket clientSocket;
	
	//total of 479 bytes, 24 datagrams * 20 bytes = 480
	//this array will be used to store the arrays created
	//from txtByte with the appended seqNum byte
	byte payLoadArray[][] = new byte[24][20];
	
	//sequence number of sent UDP datagram, 0-19, 20 datagrams should be sent
	//use as int to get the next number, then convert to byte to append to array
	byte seqNum = 0;
	byte ackYes = 1;
	
	//remember how far we have read up to in txtByte
	int temp = 0;
	
	if((args.length != 2)){
	    System.out.println("Usage: %s <Gateway IP> [<Gateway Port>]\n");
	    System.exit(1);
	}
	
	serverIP = args[0];
	
	if(args.length == 2){
	    serverPort = (new Integer(args[1])).intValue();
	}else{
	    serverPort = 7;
	}
	
	try {
	    clientSocket = new DatagramSocket();
	    serverIPAddress = InetAddress.getByName(serverIP);
	    
	    try{
		
		//read file bytes into array
		byte[] txtByte =  Files.readAllBytes(new File("CSE342.txt").toPath());
		/*
		  in txtByte[] now, we have 479 bytes from file stored
		  so next we start with a seqNum and append that to
		  every 19 bytes until we are done with the 479,
		  this should give us the 20ish UDP datagrams
		  From there, we construct a packet with the various byte arrays
		  append seqNum byte to the leading position of each byte array
		  then pass to payloadArray temp is 0, on 1st iteration we go from 0-18 to place bytes in
		  then we have to remember what was last read from txtByte...19-...
		  this should fill the payloadArray[24][20] with 25 byte arrays
		  representing the datagrams containing 20 bytes each.
		*/
		
		/*
		  go to 2nd last, then calc check from ele -- append
		  build the payload array
		*/
		while(seqNum < 24){
		    for(int j = 1; j < 20; j++){
			
			//set the first byte to the seqNum
			payLoadArray[seqNum][0] = seqNum;
			
			//then append the rest of the bytes(19 bytes)
			payLoadArray[seqNum][j] = txtByte[temp];
			
			temp++;
		    }
		    seqNum ++;
		}//while
		
		/*
		  take payload array and execute stop/wait ARQ
		  save the current seqNum, then we send a datagram to the server
		  stop and wait for an ACK from the server, in this case we will use the seqNum
		  if the serverSeqNum == the client's seqNum, we send the next datagram
		*/
		
		//create/send the first datgram to the server, then enter into while that will do stop/wait
		sendDatagramPacket = new DatagramPacket(payLoadArray[0], payLoadArray[0].length, serverIPAddress, serverPort);
		clientSocket.send(sendDatagramPacket);
		
		seqNum = 0;
		
		while(!done){
		    
		    //wait for/get return packet from server
		    clientSocket.setSoTimeout(5000);
		    
		    try{
			rcvdDatagramPacket = new DatagramPacket(rcvData, rcvData.length);
			clientSocket.receive(rcvdDatagramPacket);
			
			//received ACK, send the next packet, seqNum ++;
			seqNum++;
			
			//creat next datagram to send
			sendDatagramPacket = new DatagramPacket(payLoadArray[seqNum], payLoadArray[seqNum].length, serverIPAddress, serverPort);
			clientSocket.send(sendDatagramPacket);
			//we have gone through all the datagrams, DONE!
			if(seqNum == 23){
			    done = true;
			}
			
		    }catch(SocketTimeoutException ex){
			clientSocket.send(sendDatagramPacket);
		    }
		}//while
		
	    }catch(FileNotFoundException exf){
		System.out.println(exf);
	    }catch(IOException ioex){
		System.out.println(ioex);
	    }
	    
	    //close client socket
	    clientSocket.close();
	}catch (Exception ex){
	    System.out.println("Socket Error: " + ex);
	}
    }
}
