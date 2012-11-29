import java.io.*;
import java.net.*;

public class EchoServer
{
	public static void main(String args[])
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
			System.exit(-1);
		}
		
		try
		{
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			while( (inputLn = in.readLine()) != null)
			{
				outputLn = "[SERVER] Received: " + inputLn;
				out.println(outputLn);
				System.out.println("[CLIENT] " + inputLn);
				if(inputLn.equals("BYE"))
				{
					break;
				}
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
