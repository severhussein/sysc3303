This README file explaisn how to setup the ECLIPSE project and an explanation of the java files included.

The necessary files required to run the components as an Eclipse project 
are already included.
Please select the dropdown menu "File" -> "Import" -> "General"-> 
"Existing Projects into Workspace" in Eclipse and select the directory 
containing this readme file as the root directory.

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

Do the same for Server.java but paste into 
the working directory "${workspace_loc:sysc3303_t4/serverFileStorage}"

The working directory is now updated and read the DetailedInstructions.txt file to run the code.

*****************************************************************************
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
To run the iteration 3:

1- Run the RequestListener:
-The server has two modes, a verbose mode, and a quiet mode
-Entering 1 will toggle between these two modes
-Next, on the Server, enter option 2 to begin running the Server

2- Run the IntHostListener (Optional):
-This iteration does not require the use of error simulator, but it can be used in "no error" mode if needed

3- Run the Client:
-Inside the client there are many UI options
-you can toggle between normal and test mode, as well as quiet and verbose mode
-It is suggested to use normal mode to let client/server interact directly, but test mode can be used with error simulator if desired
-To read a file, select option 1, but first read the "FILE READ/WRITE SECTION" above
-To write a file, select option 2, but first read the "FILE READ/WRITE SECTION" above

4- Generate the error messages:
*TFTP error code 1 "File not found."
-To generate TFTP error code 1 on the server, use the client option 1 to read a file not 
available in serverFileStorage (the default server working directory)
-TFTP error code 1 cannot be sent from the client as a sanity check will be done on the UI when user enters a filename. If the file does not exist the client will simply prompt the user and return to the main menu

*TFTP error code 2 "Access violation."
-To generate TFTP error code 2 on server, change the read/write permission of the targeted by right clicking on the file icon, then select "Properties" -> "Security" tab -> "Edit”"and check the deny check boxes.
-Refer to this Microsoft TechNet article if more information is needed on setting the permission
https://technet.microsoft.com/en-us/library/cc754344(v=ws.11).aspx
-Run the client to access the file with modified permission to generate TFTP error code 2. For example, user can set a file "a.txt" on server to deny write permission, then use the client to write “a.txt” to the server. A TFTP error code 2 will be returned by the server. Alternatively user can set "b.txt" on server to deny read, then use the client to read "b.txt" and a TFTP error 2 will be returned from server as well.
-Just like TFTP error code 1, a sanity check will be done on the client before sending the request to the server. So user will be prompted about the lack of permission, with no request being sent tp server.

*TFTP error code 3 "Disk full or allocation exceeded."
-To generate TFTP error code 3 on both server and client, change the "Working Directory" mentioned in the setup section to a device with limited space. An external storage like USB flash disk is suggested.
-Run the client to write a file larger than the space available of the working directory of the server. A TFTP error code 3 is expected from the server when the storage device runs out of space. 
-Run the client to read a file larger than the space available of the working directory of the client. A TFTP error code 3 is expected from the client when the storage device runs out of space. 

*TFTP error code 6 "File already exists."
-TFTP error code is not implemented because this implementation allows overwriting existing files.

*TFTP error code 0 
-This error code is not required for this iteration, but if exception occurs due reason unknown or unhandled this TFTP error code will be used. Although unlikely to happen, please refer to the error message when such error is generated.
