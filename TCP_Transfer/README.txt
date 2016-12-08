Matthew Chin - mjc714 - CSE 342 - TCP Lab

1a. Open two terminal windows connected to different hosts.
1b. Run Makefile: make

In one of the terminal windows - 
2a. Run ifconfig to get server IP
2b. Start server with java TCPServer <Client Port>
	example: java TCPServer 2000

In another terminal window - 
3. Start client with java TCPClient <Server IP> <Server Port> <File Name>
	note: server IP is obtained from ifconfig in other terminal window.
	example: java TCPClient 128.180.120.66 2000 ascii.gif
	
4. Throughput information is printed out once file is received by the server.