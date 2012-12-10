import java.io.*;
import java.net.*;

public class RMON {
   static Agent agent;
   static Client client;
   
      public static void main(String[] args)
    {
        int port;
        if(args.length < 1)  
        {
            System.out.println("Usage: java Server_7 [port]"); 
            System.exit(1);
     }
        try{
            port = Integer.parseInt(args[0]);
            RMON s = new RMON(port);
            
        }
        catch(NumberFormatException e){
            System.out.println("Port is Integer!!");
        }
    }
    
    
    
    
    
    
    
   
     private boolean OutServer = false;
    private ServerSocket server;
    private final int ServerPort = 4445;
    
 //Create Server
    public  RMON(int port) {
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
            this.process(clientSocket);
            clientSocket.close();
 
        } catch (java.io.IOException e) {
            System.out.println("Socket Error !");
            System.out.println("IOException :" + e.toString());
        }
    }
      public void process(Socket client)
    {
        
        try{
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());
            String userInput = in.read();  //read Client 
            System.out.println("receive from client : " + userInput);
            out
        }catch(IOException e){
            System.out.println("Client out");
        }
    }
    
    
}
