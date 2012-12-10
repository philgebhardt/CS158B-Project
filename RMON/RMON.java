package RMON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

import javax.swing.WindowConstants;

public class RMON {
	
	HashMap<String, User> users;
	
	public RMON()
	{
		pullConfigInfo();
		threading();
	}
	
	private void threading()
	{
		Runnable r1 = new Runnable() {
			public void run() {
				requestHandler(4444, 4446, "169.254.9.55", "Client");
			}
		};
		Runnable r2 = new Runnable() {
			public void run() {
				requestHandler(4447, 4445, "169.254.18.18", "Agent");
			}
		};
		Thread thr1 = new Thread(r1);
		Thread thr2 = new Thread(r2);
		thr1.start();
		thr2.start();
	}
	
	private void requestHandler(int listenerPort, int senderPort, String ipDest, String listeningFor)
	{
		while(true)
	    {
			//listener socket
	        ServerSocket serverSocket = null;
	        Socket clientSocket = null;
	        OutputStream out = null;
	        InputStream in = null;
	        //sender socket
	        Socket echoSocket = null;
	        OutputStream outF = null;
	        InputStream inF = null;
	        
	        try
	        {
	            serverSocket = new ServerSocket(listenerPort);
	        }
	        catch(IOException e)
	        {
	            System.out.println("ServerSocket connection error.");
	            continue;
	        }
	        
	        try
	        {
	        	//start listening///////////////////////////////////////////////////////////
	            System.out.println("Server is listening...");
	            clientSocket = serverSocket.accept();
	            out = clientSocket.getOutputStream();
	            in = clientSocket.getInputStream();
	            
	            byte[] input, output, iv, message;
	            String name;
	            int totalBytes;
	            
	            //start reading transferred data////////////////////////////////////////////
	            input = new byte[1000];
	            totalBytes = in.read(input);
	            input = copy(input, 0, totalBytes);
	            name = new String(input);
	            name = name.substring(0, name.indexOf(' '));
	            iv = copy(input, name.length()+1, 16);
	            message = copy(input, name.length() + 16 + 1);
	            Key key;
	            if(listeningFor.equals("Client"))
	            	key = users.get(name).getRMONKey();
	            else
	            	key = users.get(name).getKey();
	            message = Crypto.AESCBCdecrypt(message, key, iv);
	            //finish reading transferred data///////////////////////////////////////////
	            //start writing data///////////////////////////////////////////////////////
	            process(new String(message), listeningFor);
	            iv = Crypto.generateIV(0, 16);
	            key = users.get(name).getRMONKey();
	            message = Crypto.AESCBCencrypt(message, key, iv);
	            output = concat(iv,message);
	            //finish writing data///////////////////////////////////////////////////////
	            //start sending data///////////////////////////////////////////////////////
	            try
	            {
	                echoSocket = new Socket(ipDest, senderPort);
	                outF = echoSocket.getOutputStream();
	                inF = echoSocket.getInputStream();
	    		    outF.write(output);
		            //finish sending data//////////////////////////////////////////////////////
		            //start receiving data////////////////////////////////////////////////////
	    		    input = new byte[1000];
	    		    totalBytes = inF.read(input);
		            //finish receiving data///////////////////////////////////////////////////
	    		    //start reading transferred data////////////////////////////////////////////
		            input = copy(input, 0, totalBytes);
		            name = new String(input);
		            name = name.substring(0, name.indexOf(' '));
		            iv = copy(input, name.length()+1, 16);
		            message = copy(input, name.length() + 16 + 1);
		            message = Crypto.AESCBCdecrypt(message, key, iv);
		            //finish reading transferred data///////////////////////////////////////////
		            //start writing data///////////////////////////////////////////////////////
		            process(new String(message), listeningFor, totalBytes);
		            iv = Crypto.generateIV(0, 16);
		            if(listeningFor.equals("Client"))
		            	key = users.get(name).getKey();
		            else
		            	key = users.get(name).getRMONKey();
		            message = Crypto.AESCBCencrypt(message, key, iv);
		            output = concat(iv,message);
		            //finish writing data///////////////////////////////////////////////////////
		            //start sending data////////////////////////////////////////////////////////
		            out.write(output);
		            //finish sending data///////////////////////////////////////////////////////
	            }
	            catch (UnknownHostException e)
	            {
	                System.out.println("Don't know about host: " + echoSocket.getInetAddress());
	            }
	            catch (IOException e)
	            {
	                System.out.println("Couldn't get I/O for the connection to: " + ipDest);
	                
	            }
	            try
	            {
	    		    out.close();
	        		in.close();
	        		echoSocket.close();
	            }
	            catch (IOException e)
	            {
	            	System.out.println("Couldn't get I/O for the connection to: " + output);
	            }
	            
	        }
	        catch(IOException e)
	        {
	            System.out.println("Client socket acceptance error.");
	            System.exit(-1);
	        }
	        catch(Exception e)
	        {
	            System.out.format("%s%n", e.getMessage());
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
	private String process(String toProcess, String listeningFor)
	{
		return toProcess;
	}
	private String process(String toProcess, String listeningFor, int totalBytes)
	{
		return toProcess;
	}
	
	private byte[] copy(byte[] a, int offset, int len)
	{
	    byte[] rv = new byte[len];
	    for(int i = 0; i < len; i++) rv[i] = a[offset + i];
	    return rv;
	}
	private byte[] copy(byte[] a, int offset)
    {
        byte[] rv = new byte[a.length - offset];
        for(int i = 0; i < a.length - offset; i++) rv[i] = a[offset + i];
        return rv;
    }
	
	public void pullConfigInfo()
	{
	    String line;
	    users = new HashMap<String, User>();
	    FileSystem fs = FileSystems.getDefault();
	    Path configFile = fs.getPath("CONFIG");
	    
	    try
	    {
	        InputStream in = Files.newInputStream(configFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        
	        line = null;
	        while(!(line = reader.readLine()).equals("USERS"));
	        while((line = reader.readLine()) != null)
	        {
	           createUser(line);
	        }
	        
	        in.close();
	        reader.close();
	    }
	    catch(IOException e)
	    {
	        System.err.format("Error reading from CONFIG file: %s%n", e.getMessage());
	    }
	    catch(NoSuchElementException e)
	    {
	        System.err.format("Error reading line from CONFIG file: %s%n", e.getMessage());
	    }
	}
	public void createUser(String input)
	{
	    StringTokenizer st;
        String name;
        byte[] keyBytes = new byte[16];
        byte[] rmonBytes = new byte[16];
        st = new StringTokenizer(input, " ");
        name = st.nextToken();
        for(int i = 0; i < 16; i++) keyBytes[i] = (byte) Integer.parseInt(st.nextToken());
        for(int i = 0; i < 16; i++) rmonBytes[i] = (byte) Integer.parseInt(st.nextToken());
        users.put(name, new User(name, new Key(keyBytes), new Key(rmonBytes)));
	}
	private byte[] concat(byte[] a, byte[] b)
	{
	    byte[] rv = new byte[a.length + b.length];
	    for(int i = 0; i < a.length; i++) rv[i] = a[i];
	    for(int i = 0; i < b.length; i++) rv[i + a.length] = b[i];
	    return rv;
	}
}
