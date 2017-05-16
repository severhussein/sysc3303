This README file explaisn how to setup the ECLIPSE project and an explanation of the java files included.

The necessary files required to run the components as an Eclipse project 
are already included.
Please select the dropdown menu "File" -> "Import" -> "General"-> 
"Existing Projects into Workspace" in Eclipse and select the directory 
containing this readme file as the root directory (sysc3303-iteration1).

Once the project is imported, the components can be launched easily via Eclipse.
Simply navigate to the dropdown menu "Run" -> "Run Configuration" to start each 
components individually using the included run configurations.

*****************:SINCE THE PROF HAS NOW ALLOWED .LAUNCH FILES, THESE STEPS ARE NO LONGER NECESSARY:**********************
Run the Client.java class, then exit the java aplication.
Then:
Select "Run"->"Run Configurations"
On the left hand side there is a list of Java Applications, ensure it is on Client
Select the tab "Arguments" near the top 
For the "Working Directory:"
-Select "other" and paste "${workspace_loc:sysc3303_t4/clientFileStorage}"
-Hit "Apply" to apply the changes
-Finally Hit "close"

Do the same for RequestLisener.java but paste into 
the working directory "${workspace_loc:sysc3303_t4/serverFileStorage}"

The working directory is now updated and read the DetailedInstructions.txt file to run the code.
*****************************************************************************
By default the client has the working directory set to the "clientFileStorage"
folder in the project, while server has it set to "serverFileStorage" folder. 
If absolute path is not provided, then the components will use these directories
as the reference points. 

Alternatively, user can choose create their own launch configuration.
The main method of the client is located in Client class.
The main method of the error simulator is located in IntHostListener class.
The main method of the server is located in RequestListener class.
Be aware that eclipse default the working directory to the root of the project
folder, user must modify the Run configuration just created to 
prevent client/server from accessing the same file at the same time.

Refer to the link below for a guide on creating launch configuration and 
changing the working directory.

http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Ftasks%2Ftasks-java-local-configuration.htm

An Alternative to getting the project files from the submit program is to download directly from our git.
Our URL is https://github.com/severhussein/sysc3303.git. You may enter the URL into a browser and download it there
or you may open up eclipse and following the import steps previously mentioned, instead of choosing
General->Existing Project, you may choose Git->Projects From Git and select Clone URI option. Then input out git URL
and eclipse will automatically download the repository.

Brief explanation of the source directory:

TftpPacketHelper\
	A directory containing TFTP help classes which may be used in the future
Client.java
	Contains the implementation of the client component
CommonConstants.java
	Contains the constants used in the components
Helper.java
	Contains helper methods
IntHostListener.java
	Contains the implementation of the rest of packet forwarding mechanism
IntHostManager.java
	Contains the implementation the the error simulator
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
