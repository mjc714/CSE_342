// Matthew Chin - mjc714
// CSE 342
// TCP Server

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class TCPServer {

    public static void main(String args[]) throws IOException {

        int clientPort;
        int bytesRead;
        int count = 0;

        if (args.length != 1) {
            System.out.println("Usage: <Client Port>\n");
            System.exit(1);
        }

        clientPort = Integer.parseInt(args[0]);
        // try to connect to client
        try {
            long start = System.nanoTime();
            // create sockets for reading and writing to server
            ServerSocket serverSocket = new ServerSocket(clientPort);
            Socket clientSocket = serverSocket.accept();

            // open input stream for reading data from client
            InputStream inputStream = clientSocket.getInputStream();
            DataInputStream clientDataIn = new DataInputStream(inputStream);

            // get file name from client stream
            String fileName = clientDataIn.readUTF();
            fileName = fileName + "_Rcvd";
            // open output stream for writing file out to server side
            OutputStream outStream = new FileOutputStream(fileName);
            long fileSize = clientDataIn.readLong();
            long sizeTrack = fileSize; // this is to store the file size for throughput measurement\

            // create byte array for reading contents into
            byte[] bufIn = new byte[1024];
            // read file contents in
            while (fileSize > 0 && (bytesRead = clientDataIn.read(bufIn, 0, (int) Math.min(bufIn.length, fileSize))) != -1) {
                outStream.write(bufIn, 0, bytesRead);
                fileSize -= bytesRead;
            }

            // close streams and sockets
            serverSocket.close();
            clientSocket.close();
            inputStream.close();
            clientDataIn.close();
            outStream.close();
            long end = System.nanoTime();
            long elapsedTime = end - start;
            elapsedTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            long throughput = (sizeTrack / elapsedTime);
            System.out.println("TCP Throughput: " + throughput + " bytes/second.");
        } catch (IOException ioex) {
            System.err.println("Could not connect to client port: " + clientPort);
            System.exit(1);
        }
    }
}
