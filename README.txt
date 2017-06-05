This README file explains how to setup the ECLIPSE project, the behaviour of the system, test cases, and an 
explanation of the java files included.

The necessary files required to run the components as an Eclipse project 
are already included. The basic idea of this project entails a client server interaction 
allowing file transfer following RFC 1350 protocols. There is also an error simulator included
to test out different error scenarios. First, lets setup the Eclipse Project.

****************************SETTING UP ECLIPSE PROJECT***********************************

Open Eclipse. In Eclipse select the dropdown menu "File" -> "Import" -> "General"-> 
"Existing Projects into Workspace" and select the root directory as
the folder containing this REAMDE file.

Once the project is imported, the components can be launched easily via Eclipse.
On the left pane of Eclipse, there is the "Package Explorer" navigation tool. Navigate to
the project folder as such: "sysc3303_t4" folder -> "src" folder -> "default package".

There are three runnable java applications that will concern you:

1- Client.java
2- ErrorSimulator.java
3- Server.java

The order in which you run these java classes are: 3, 2, 1. However, 3 and 2 can be 
interchanged.

****************
TIP: It is best to read the behaviour of this system, located below, before running these applications.
****************

To run these applications from Eclipse, simply double click on the java file and 
hit "ctrl+F11" or go to the drop down menu located on the top of Eclipse and select
"Run"-> "Run". For example:

-double click on Server.java
-hit Ctrl+F11; or,
-select "Run"-> "Run" from the drop down menu

If setting up Eclipse did not work using this method, there is an alternative method located
at the bottom of this README. 

****************************STORAGE FOR CLIENT/SERVER***********************************
By default the client has the working directory set to the "clientFileStorage"
folder in the project, while server has it set to "serverFileStorage" folder. 
This means that when transferring files, they will be located in these folders after 
the transfer. That being said:
-If you would like to write a file to server, make sure the file is inside the folder "clientFileStorage"
-If you would like to read a file from server, make sure the file is inside the folder "serverFileStorage"

***************************BEHAVIOUR OF SYSTEM DOCUMENTATION*****************************
This project will work in the CB5109/CB5107/ME4233 lab rooms however it should be noted:
The required Java version is 1.8. This project should run on Java 1.7 as well, but it is not recommended 
as the verification are done on Java 1.8 only.


In the RFC 1350 protocol, the types and support for the errors are listed below:

Support for the errors:

Err. code  Type
 0         Not defined, see error message (if any).
 1         File not found.
 2         Access violation.
 3         Disk full or allocation exceeded.
 4         Illegal TFTP operation.              
 5         Unknown transfer ID.              
 6         File already exists.             Allows overwrite
 7         No such user.                    Deprecated
 

-If the system encounters erro code 3, disk full or allocation exceeded, during a file transfer, the uncompleted file will be deleted.
This is to reduce the chance of affecting regular operations of the sytem.
-Error code 6, file already exists, is not implemented because the system will overwrite files. This is our implementation decision.
If the client chooses to send a file with the same name then the client must change the name before.
-Error code 7, is deprecated, and is not implemented in this system.



Other behavior choices:
-The system will not allow writing to a file being read.
-The system will not allow reading a file when it is being written to.
-The system will not allow identical files being written concurrently.

-The error simulator has been setup so that it can be deployed on the same machine as the client, server, or on a seperate machine. However,
we recommened that you choose to do it on the same machine as the Client.

-Timeouts on receiving acknowledges are 5 seconds. If an acknowledge is not received, then the corresponding data block will be resent upto 5 times. 
After 5 retries, the request will timeout and the connection will close. The client must request a new connection.

-The project can all be run on the same computer but this is not recommended because it does not simulate a real client server environment. 

The remaining behaviors of the system follow the RFC 1350 protocol.


**************************************TEST CASES TO SIMULATE****************************************

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
	



***********************************EXPLANATION OF JAVA FILES************************************

├───src
│   │   Client.java			-The client side application that allows RRQ and WRQ to be sent to server
│   │   CommonConstants.java		-A class for holding common constants used throughout the project
│   │   ErrorSimulator.java		-The Class that allows to run different error scenarios between client/server
│   │   ErrorSimulatorHelper.java	-A helper class for the Error Simulator
│   │   ErrorSimulatorThread.java	-The Error Simulator thread class, after each error simulator request, a new thread is opened
│   │   InvalidPacketException.java	-A class that handles error for invalid packets
│   │   Server.java			-The server side application that handles RRQ and WRQ from client
│   │   ServerScanner.java		-This class handles shutdown requests from the server console
│   │   ServerThread.java		-The thread that is created for each new RRQ/WRQ
│   │   Utils.java			-Utility functions for the Client and Server
│   │
│   └───TftpPacketHelper		-This package is a helper package to decode different types of packets (ack,error,data, etc.)
│           TftpAckPacket.java
│           TftpDataPacket.java
│           TftpErrorPacket.java
│           TftpOackPacket.java
│           TftpPacket.java
│           TftpReadRequestPacket.java
│           TftpRequestPacket.java
│           TftpWriteRequestPacket.java


***************ALTERNATIVE METHOD FOR SETTUPING UP ECLIPSE PROJECT**********************
An Alternative to getting the project files from the submit program is to download directly from our git.
Our URL is https://github.com/severhussein/sysc3303.git. You may enter the URL into a browser and download it there
or you may open up eclipse and following the import steps previously mentioned, instead of choosing
General->Existing Project, you may choose Git->Projects From Git and select Clone URI option. Then input our git URL
and eclipse will automatically download the repository.

*****************************************************************************************

