import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.IOException;

/**
 * IntHostManager for multi threads
 * need Helper.java
 * @author Dawei Chen 101020959
 */

public class IntHostManager implements Runnable {

    private DatagramSocket socket;
    private DatagramPacket sendPacket, receivePacket;
    int clientPort, serverPort;

    public IntHostManager(DatagramPacket receivePacket) {
        socket = Helper.newSocket();
        if (socket!= null) {
            this.serverPort = Helper.DEFAULT_SERVER_PORT;
            this.clientPort = receivePacket.getPort();
            this.receivePacket = receivePacket;
        }
    }

    
    public void run() {
        int type = IntHostListener.mode;//promt user which error want to simulate
        
        boolean server_port_needed = true;
        
        //create error on the 2nd pass of the file transfer
        int i = 0;
        while(true) {
                      

                if (i == 2 && type == 2) {
                    simulate_wrong_port(1);//1 is to server
                } else if (i == 2 && type == 4) {
                    simulate_wrong_opcode(1);//1 is to server
                } else {        
                     Helper.print("Host sending to server...\n");
                     sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
                     Helper.send(socket, sendPacket);
                     Helper.printPacket(sendPacket);
                 }

            
            i++;//first round of request msg was done, increase i here
            
            Helper.print("Host receiving from server...\n");
            Helper.receive(socket, receivePacket);
            Helper.printPacket(receivePacket);
            if (server_port_needed) {server_port_needed = false; serverPort = receivePacket.getPort();}//get server port here!


                if (i == 2 && type == 1) {
                    simulate_wrong_port(1);//1 is to server
                } else if (i == 2 && type == 3) {
                    simulate_wrong_opcode(1);//1 is to server
                } else {  
                    Helper.print("Host sending to Client...\n");
                    sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
                    Helper.send(socket, sendPacket);
                    Helper.printPacket(sendPacket);
                 }

            
            Helper.print("Host waiting for client data...\n");
            Helper.receive(socket, receivePacket);
            Helper.printPacket(receivePacket);
        }
    }
    

    
    public void simulate_wrong_port(int direction) {
        
        DatagramSocket new_socket = Helper.newSocket();
        
        if (direction == 1) {
            Helper.print("simulate ERROR 5: Host sending to server...\n");
            sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
            Helper.send(new_socket, sendPacket);
            Helper.printPacket(sendPacket);
        }
        else if (direction == 0) {
            Helper.print("simulate ERROR 5: Host sending to Client...\n");
            sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
            Helper.send(new_socket, sendPacket);
            Helper.printPacket(sendPacket);
        }

    }
    public void simulate_wrong_opcode(int direction) {
        
        if (direction == 1) {
            Helper.print("simulate ERROR 4: Host sending to server...\n");
            receivePacket.getData()[0] = 7;
            sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
            Helper.send(socket, sendPacket);
            Helper.printPacket(sendPacket);
        }
        else if (direction == 0) {
            Helper.print("simulate ERROR 4: Host sending to Client...\n");
            receivePacket.getData()[0] = 7;
            sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
            Helper.send(socket, sendPacket);
            Helper.printPacket(sendPacket);
        }
    }
    
}

