import java.io.*;
import java.net.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author TwFoB
 */
public class Agent extends Thread{
        private final int ServerPort;
        private final int ClientPort;
        private String address = "192.168.1.254";

    /**
     *
     */
    public Agent(){
        this.ServerPort = 4446;
        this.ClientPort = 4447;
        
    }
    @Override
    public void run(){
           try {
             ServerSocket server = new ServerSocket(ServerPort);
            System.out.println("start to waiting...");
            Socket clientSocket = server.accept();  //receive a client 
            java.util.Date currentDate = new java.util.Date();
            /*** print client***/
            System.out.println("-----  Accept a client  -----");
            System.out.println("Clinet IP: " + clientSocket.getInetAddress().getHostAddress());//Client IP
            System.out.println("Client Port: " + clientSocket.getPort());  //Client Port
            System.out.println("connect date: " + currentDate); 
            //sent to Agent
            Socket client = new Socket();
            InetSocketAddress isa = new InetSocketAddress(this.address,this.ClientPort);
            try{
                client.connect(isa, 10000); // endpoint time out 
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                //read and write 
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                 byte[] b = new byte[1024];
                 String data = "";
                 int length;
            while ( (length = in.read(b))>0); //read Client 
                 data += new String(b, 0, length);
             System.out.println("receive from client : " + data);
             //write out
            out.write(data.getBytes());
            }catch (java.io.IOException e) {
                System.out.println("Client Socket Connection Error !");
                System.out.println("IOException :" + e.toString());
            }
            
 
        } catch (java.io.IOException e) {
            System.out.println("Socket Error !");
            System.out.println("IOException :" + e.toString());
        }
        
    }
}
