import java.io.*;
import java.net.*;

public class EchoClient
{
    public static void main(String[] args) throws IOException 
    {

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        
        System.out.print("Server address: ");
        userInput = stdIn.readLine();
        
        try
        {
            echoSocket = new Socket(userInput, 4444);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
            while ((userInput = stdIn.readLine()) != null)
    		{
    		    out.println(userInput);
    		    System.out.println("echo: " + in.readLine());
    		    if(userInput.equals("BYE"))
    		    {
    		    	break;
    		    }
    		}
        }
        catch (UnknownHostException e)
        {
            System.out.println("Don't know about host: " + echoSocket.getInetAddress());
        }
        catch (IOException e)
        {
            System.out.println("Couldn't get I/O for the connection to: " + userInput);
        }
        finally
        {
        	out.close();
    		in.close();
    		stdIn.close();
    		echoSocket.close();
        }
	}
}