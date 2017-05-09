How to run the code:

The launch configuration for the three components are already 
include as part of the ellipses project.

Simple import the project, and click on the dropdown menu 
Run -> Run Configuration and start each components individually.

By default the client has the working directory set to the “clientFileStorage”
in the project while server has it set to “serverFileStorage”. 
If absolute path is not provided then the component will use 
these directories as reference point. 

A test file acronyms.txt is included for your convenience.

Alternatively, user can choose to do a right click, then select “Run As” -> “Java Application” 
on the source code file containing the main method instead.


Brief explanation of the source directory:
TftpPacketHelper\
	A directory containing help classes which may be used in the future.
Client.java
	Contains the implementation of the client component
CommonConstants.java
	Contains the constants in the components
Helper.java
	Contains helper methods
HostListener.java
	Contains the main method and thread spawner of the error simulator
HostSender.java	add missing file
	Contains the implementation of the rest of packet forwarding mechanism
IntHostListener.java
	Contains the implementation of the rest of packet forwarding mechanism
IntHostManager.java
	Contains another implementation the rest of client/server interaction
InvalidPacketException.java
	Custom exception to indicate the reception of an invalid packet
RequestListener.java
	Contains the main method and thread spawner of the server
RequestManager.java
	Contains the implementation of TFTP client/server interaction 
RequestPacket.java
	Helper class for decoding/encoding TFTP request packet
Utils.java
	Contains helper methods