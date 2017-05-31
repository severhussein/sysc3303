This README file explaisn how to setup the ECLIPSE project and an explanation of the java files included.

The necessary files required to run the components as an Eclipse project 
are already included.
Please select the dropdown menu "File" -> "Import" -> "General"-> 
"Existing Projects into Workspace" in Eclipse and select the directory 
containing this readme file as the root directory.

Once the project is imported, the components can be launched easily via Eclipse.
Simply navigate to the dropdown menu "Run" -> "Run Configuration" to start each 
components individually using the included run configurations.

*****************: FOLLOWING STEPS ARE ONLY OPTIONAL:**********************
(If user prefer not using "Run Configuration" above, following way is then needed.)

Run the Client.java class, then exit the java aplication.
Then:
Select "Run"->"Run Configurations"
On the left hand side there is a list of Java Applications, ensure it is on Client
Select the tab "Arguments" near the top 
For the "Working Directory:"
-Select "other" and paste "${workspace_loc:sysc3303_t4/clientFileStorage}"
-Hit "Apply" to apply the changes
-Finally Hit "close"

Do the same for Server.java but paste into 
the working directory "${workspace_loc:sysc3303_t4/serverFileStorage}"

The working directory is now updated and read the DetailedInstructions.txt file to run the code.

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
General->Existing Project, you may choose Git->Projects From Git and select Clone URI option. Then input out git URL
and eclipse will automatically download the repository.

*******************************************************************************
To run the iteration 4:

1- Run the Server.java
-The server has two modes, a verbose mode, and a quiet mode, (Default at Verbose Mode)  (see note 1 below)
-Next, on the Server, enter option 2 to begin running the Server

2- Run the ErrorSimulator.java
- To simulate Packet Duplicate at Data#3, choose options by the sequence: {2, 1, 1, 3, 3} (detail and other cases see Case Steps below)

3- Run the Client.java:
-You can toggle between normal and test mode, as well as quiet and verbose mode. (Default at Verbose Mode + Test Mode) (see note 2, 3 below)
-To read a file, select option 1, but first read the "FILE READ/WRITE SECTION" above
-To write a file, select option 2, but first read the "FILE READ/WRITE SECTION" above

<Case Steps to Simulator Network Errors>

Packet Duplicate (Data packet #3 as example)
	(i) Run ErrorSimulator.java, choose options by the sequence: {2, 1, 1, 3, 3}
	* The first 2 options has to be {2, 1}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "2" (begin server). Then the server is ready.
	(iii a read file) Run Client.java, choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.

Packet Delayed (Data packet #3, 7000 ms delay as example)
	(i) Run ErrorSimulator.java, choose options by the sequence: {2, 2, 7000, 3, 3}
	* This will delay for 7000 ms, which is higher than 5000 ms threshold, and transfer will timeout.
	* The first 2 options has to be {2, 2}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "2" (begin server). Then the server is ready.
	(iii a read file) Run Client.java, choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.

Packet Lost (Data packet #3 as example)
	(i) Run ErrorSimulator.java, choose options by the sequence: {2, 3, 1, 3, 3}
	* The first 2 options has to be {2, 3}, but the last 3 options can be changed according to user's need.
	(ii) Run Server.java, choose "2" (begin server). Then the server is ready.
	* Before doing next step, make sure everything on Server and ErrorSimulator was set, or the Client will not get response and act like request packet lost.
	(iii a read file) Run Client.java, choose "1" (Read), then type "serverFile.txt". Then the transfer will be started.
	(iii b write file) Run Client.java, choose "2" (Write), then type "clientFile.txt". Then the transfer will be started.


<Notes about UI settings>

Note 1 (Disable/Toggle Server Verbose mode):
	By default, Verbose mode is ON.
	In Server console, it ask user to choose from 2 options at the begining.
	Choose 1 (Toggle mode).

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
	Choose 5 (shutdown).

Note 5 (Shutdown Server):
	Type "shutdown" in Server console. It will shut down after the current transfer finished.
	*During a large file is transfering, if server is in Verbose mode (default), you may have difficulty to type in console, since message refreshes so quickly.
	*To avoid this, use Quite Mode on server.