This README file explains how to setup the ECLIPSE project and an explanation of the java files included.

The necessary files required to run the components as an Eclipse project 
are already included.
Please select the dropdown menu "File" -> "Import" -> "General"-> 
"Existing Projects into Workspace" in Eclipse and select the directory 
containing this readme file as the root directory.

Once the project is imported, the components can be launched easily via Eclipse.
Simply navigate to the dropdown menu "Run" -> "Run Configuration" to start each 
components individually using the included run configurations.


****************************<FILE READ/WRITE>***********************************
By default the client has the working directory set to the "clientFileStorage"
folder in the project, while server has it set to "serverFileStorage" folder. 
If absolute path is not provided, then the components will use these directories
as the reference points. 

Alternatively, user can choose create their own launch configuration.
The main method of the client is located in Client class.
The main method of the error simulator is located in ErrorSimulator class.
The main method of the server is located in Server class.
Be aware that eclipse default the working directory to the root of the project
folder, user must modify the Run configuration just created to 
prevent client/server from accessing the same file at the same time.

Refer to the link below for a guide on creating launch configuration and 
changing the working directory.

http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Ftasks%2Ftasks-java-local-configuration.htm

An Alternative to getting the project files from the submit program is to download directly from our git.
Our URL is https://github.com/severhussein/sysc3303.git. You may enter the URL into a browser and download it there
or you may open up eclipse and following the import steps previously mentioned, instead of choosing
General->Existing Project, you may choose Git->Projects From Git and select Clone URI option. Then input our git URL
and eclipse will automatically download the repository.

*******************************************************************************
To run the system:

1- Run the Server.java
-The server has two modes, a verbose mode, and a quiet mode, (Default at Verbose Mode)  (see note 1 below)
-Next, on the Server, choose option 1 to begin running the Server

2- Run the ErrorSimulator.java
- Fisrt, enter the server ip address. If the error simulator and server are running on the same machine, input 1 for local machine.
- Then choose error cases based on what you want, "Incorrect Data" includes data fields, size, TID errors, "Network Error" includes duplicate, delayed and lost.
- For example, to simulate Packet Duplicate at Data#3, choose options by the sequence: {2, 1, 1, 3, 3} (detail and other cases see Case Steps below)

3- Run the Client.java:
- Choose "change IP", then input the ip address of the error simulator. If the error simulator and client are running on the same machine, input 1 for local machine.
-You can toggle between normal and test mode, as well as quiet and verbose mode. (Default at Verbose Mode + Test Mode) (see note 2, 3 below)
-To read a file, select option 1, but first read the "FILE READ/WRITE SECTION" above
-To write a file, select option 2, but first read the "FILE READ/WRITE SECTION" above

<Case Steps to Simulator Network Errors>

Packet Duplicate (Data packet #3 as example)
	(i) Run ErrorSimulator.java, input server ip, then choose options by the sequence: {2, 1, 1, 3, 3}
	* The first 2 options has to be {2, 1}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "1" (begin server). Then the server is ready.
	(iii a read file) Run Client.java, set host ip, then choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, set host ip, then choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.

Packet Delayed (Data packet #3, 7000 ms delay as example)
	(i) Run ErrorSimulator.java, input server ip, then choose options by the sequence: {2, 2, 7000, 3, 3}
	* This will delay for 7000 ms, which is higher than 5000 ms threshold, and transfer will timeout.
	* The first 2 options has to be {2, 2}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "1" (begin server). Then the server is ready.
	(iii a read file) Run Client.java, set host ip, then choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, set host ip, then choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.

Packet Lost (Data packet #3 as example)
	(i) Run ErrorSimulator.java, input server ip, then choose options by the sequence: {2, 3, 1, 3, 3}
	* The first 2 options has to be {2, 3}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "1" (begin server). Then the server is ready.
	* Before doing next step, make sure everything on Server and ErrorSimulator was set, or the Client will not get response and act like request packet lost.
	(iii a read file) Run Client.java, set host ip, then choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, set host ip, then choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.


<Notes about UI settings>

Note 1 (Disable/Toggle Server Verbose mode):
	By default, Verbose mode is ON.
	In Server console, it ask user to choose from 2 options at the begining.
	Choose 2 (Toggle mode).

Note 2 (Disable/Toggle client Verbose mode):
	By default, Verbose mode is ON.
	In Client console, when it asking to choose from 5 options at the begining or each time it done a file transfer.
	Choose 3 (toggle output mode).

Note 3 (Disable/Toggle client Testing mode):
	By default, Testing mode is ON.
	In Client console, when it asking to choose from 5 options at the begining or each time it done a file transfer.
	Choose 4 (toggle operation mode).
	* Result: Client will ignore the Error Simulator and send directly to the Server, and Server will response directly back to Client.

Note 4 (Shutdown Client):
	In Client console, when it asking to choose from 5 options at the begining or each time it done a file transfer.
	Choose 6 (shutdown).

Note 5 (Shutdown Server):
	Type "shutdown" in Server console. It will shut down after the current transfer finished.
	*During a large file is transfering, if server is in Verbose mode (default), you may have difficulty to type in console, since message refreshes so quickly.
	*To avoid this, use Quite Mode on server.
	
Note 6 (Restart ErrorSimulator / change error case):
	Just hit the terminate red square button and restart it. According to project specification, it is fine to restart error simulator this way.
	

Support for the errors:

 0         Not defined, see error message (if any).
 1         File not found.
 2         Access violation.
 3         Disk full or allocation exceeded.
 4         Illegal TFTP operation.              
 5         Unknown transfer ID.              
 6         File already exists.             Allows overwrite
 7         No such user.                    Deprecated
 

The required Java version is 1.8. This project should run on Java 1.7 as well, but it is not recommended as the verification are done on Java 1.8 only.
If a disk full error (error code 3) is encountered during a file transfer, the uncompleted file will be removed to reduce the chance of affecting regular operation of the system.
Timeouts on receiving acknowledges are 5 seconds. If an acknowledge is not received, then the corresponding data block will be resent upto 5 times. After 5 retries, the request will timeout and the connection will close. The client must request a new connection.

The error simulator can be deployed on the same machine as client, or server. It can also be deployed on a separate machine. However we suggest deploying the error simulator on the same machine as the client.
This server implementation supports overwriting existing files, so no error 6 will ever be expected.

Not allow error code other than the ones noted in RFC1350
Not allowing writing into file being read, not allowing reading into file being written.
Not allowing file being write concurrently


├───src
│   │   Client.java
│   │   CommonConstants.java
│   │   ErrorSimulator.java
│   │   ErrorSimulatorHelper.java
│   │   ErrorSimulatorThread.java
│   │   InvalidPacketException.java
│   │   Server.java
│   │   ServerScanner.java
│   │   ServerThread.java
│   │   Utils.java
│   │
│   └───TftpPacketHelper
│           TftpAckPacket.java
│           TftpDataPacket.java
│           TftpErrorPacket.java
│           TftpOackPacket.java
│           TftpPacket.java
│           TftpReadRequestPacket.java
│           TftpRequestPacket.java
│           TftpWriteRequestPacket.java
