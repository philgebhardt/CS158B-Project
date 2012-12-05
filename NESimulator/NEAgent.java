import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class NEAgent extends Thread
{
	OrderedTree<OID> data;
	
	public NEAgent(OrderedTree<OID> data)
	{
		this.data = data;
	}
	@Override
	public void run()
	{
	    while(true)
	    {
	        ServerSocket serverSocket = null;
	        Socket clientSocket = null;
	        PrintWriter out = null;
	        BufferedReader in = null;
	        String inputLn, outputLn;
	        
	        try
	        {
	            serverSocket = new ServerSocket(4444);
	        }
	        catch(IOException e)
	        {
	            System.out.println("ServerSocket connection error.");
	            continue;
	        }
	        
	        try
	        {
	            System.out.println("Server is listening...");
	            clientSocket = serverSocket.accept();
	            out = new PrintWriter(clientSocket.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	            
	            if( (inputLn = in.readLine()) != null)
	            {
	                System.out.println("Received: " + inputLn);
	                outputLn = "echo: " + inputLn;
	                out.println(outputLn);
	            }
	        }
	        catch(IOException e)
	        {
	            System.out.println("Client socket acceptance error.");
	            System.exit(-1);
	        }
	        
	        try
	        {
	            out.close();
	            in.close();
	            serverSocket.close();
	            clientSocket.close();
	        }
	        catch(IOException e)
	        {
	            System.out.println("Something happened when tearing down sockets and I/O.");
	            System.exit(-1);
	        }
	    }
	}
}
