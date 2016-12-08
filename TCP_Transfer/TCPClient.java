// Matthew Chin - mjc714
// CSE 342
// TCP Client

import java.io.*;
import java.net.*;

public class TCPClient {

    public static void main(String args[]) throws IOException {

        String serverIP;
        String fileName;

        int serverPort;

        // check args input for proper count
        if ((args.length != 3)) {
            System.out.println("Usage: <Server IP> <Server Port> <File Name>\n");
            System.exit(1);
        }

        serverIP = args[0];
        serverPort = Integer.parseInt(args[1]);
        fileName = args[2];

        // load file in
        File sendFile = new File(fileName);
        boolean fileExists = sendFile.exists();
        if (fileExists) {
            // read file into byte array
            byte[] fileByteArray = new byte[(int) sendFile.length()];

            // try to connect to server
            try {
                // open socket
                Socket clientSocket = new Socket(serverIP, serverPort);

                // read file contents into file stream
                FileInputStream sendFileStream = new FileInputStream(fileName);

                // convert to buffered file stream
                BufferedInputStream bufFileStream = new BufferedInputStream(sendFileStream);

                // convert to data input stream
                DataInputStream dInStream = new DataInputStream(bufFileStream);
                // read data stream into byte array
                dInStream.readFully(fileByteArray, 0, fileByteArray.length);

                // create output stream to send file contents to the server
                OutputStream outStream = clientSocket.getOutputStream();

                // send data to output stream for sending file info to server
                DataOutputStream dOutStream = new DataOutputStream(outStream);
                dOutStream.writeUTF(sendFile.getName());
                dOutStream.writeLong(fileByteArray.length);
                dOutStream.write(fileByteArray, 0, fileByteArray.length);
                dOutStream.flush();

                // write contents to output stream to server
                outStream.write(fileByteArray, 0, fileByteArray.length);
                // clear output stream buffer after sending
                outStream.flush();

                // close streams and socket
                sendFileStream.close();
                bufFileStream.close();
                dInStream.close();
                dOutStream.close();
                outStream.close();
                clientSocket.close();
            } catch (UnknownHostException uhex) {
                System.err.println("Unknown host: " + serverIP);
                System.exit(1);
            } catch (IOException ioex) {
                System.err.println("Could not connect to " + serverIP);
                System.exit(1);
            }
        } else {
            System.err.println("File does not exist");
            System.exit(1);
        }
    }
}
