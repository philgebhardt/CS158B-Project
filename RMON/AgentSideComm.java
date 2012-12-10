package RMON;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import Crypto.*;
import NESimulator.TrapHandler;
import Structure.Byte;
import Structure.User;

public class AgentSideComm extends Thread
{
    HashMap<String, Alarm> alarms;
    HashMap<String, User> users;
    ServerSocket serverSocket;
    Socket clientSocket;
    InputStream in;
    OutputStream out;

    Socket echoSocket = null;
    OutputStream outF = null;
    InputStream inF = null;
    Key key;
    
    public AgentSideComm(HashMap<String, User> users, HashMap<String, Alarm> alarms)
    {
        super();
        this.alarms = alarms;
        this.users = users;
    }
    
    public void run()
    {
    	while(true)
    	{
	        try
	        {
	            serverSocket = new ServerSocket(4446);
	            clientSocket = serverSocket.accept();
	            in = clientSocket.getInputStream();
	            out = clientSocket.getOutputStream();
	            
	            byte[] input, output, iv;
	            String name, destIp, proccessed;
	            //srcIp = clientSocket.getInetAddress().toString().substring(1);
	            input = new byte[1000];
	            int totalBytes = in.read(input);
	            
	            iv = Byte.copy(input, 16);
	            input = Byte.copy(input, 16, totalBytes - 16);
	            input = Crypto.AESCBCdecrypt(input, users.get("RMON").getKey(), iv);
	            process(new String(input));
	            
	            
	            in.close();
	            out.close();
	            clientSocket.close();
	            serverSocket.close();
	            
	        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | ShortBufferException | IllegalBlockSizeException | BadPaddingException e)
	        {
	            e.printStackTrace();
	        }
    	}
    }
    
    private void process(String input)
    {
        StringTokenizer st = new StringTokenizer(input, " ");
        int n = st.countTokens();
        String args[] = new String[n];
        int i = 0;
        byte[] iv;
        byte[] message;
        byte[] output;
        while(st.hasMoreTokens()) args[i++] = st.nextToken();
        int totalBytes;

        StringBuilder toReturn = new StringBuilder();
        Date date = new Date(Integer.parseInt(args[0]));
        toReturn.append(date.toString() + ": ");
        toReturn.append("Alarm " + args[2]);
        if(alarms.get(args[2]).has_crossed())
        {
        	alarms.get(args[2]).threshold_rescinded();
        	toReturn.append("rescinded, ");
        }
        else
        {
        	alarms.get(args[2]).threshold_crossed();
        	toReturn.append("crossed, ");
        }
        toReturn.append(args[6]);
        message = toReturn.toString().getBytes();
        
       
        iv = Crypto.generateIV(0, 16);
        key = alarms.get(args[2]).getUser().getKey();
        try
        {
        	message = Crypto.AESCBCencrypt(message, key, iv);
        }
        catch(Exception e)
        {
        	System.out.println(e.getMessage());
        }
    	output = concat(iv,message);
        try
        {
            echoSocket = new Socket(alarms.get(args[2]).getIpForward(), 4444);
            outF = echoSocket.getOutputStream();
            inF = echoSocket.getInputStream();
		    outF.write(output);
		    Thread.sleep(2000);
        }
        catch (UnknownHostException e)
        {
            System.out.println("Don't know about host: " + echoSocket.getInetAddress());
        }
        catch (Exception e)
        {
            System.out.println("Couldn't get I/O for the connection to: " + alarms.get(args[2]).getIpForward());
            
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
    
    private byte[] concat(byte[] a, byte[] b)
	{
	    byte[] rv = new byte[a.length + b.length];
	    for(int i = 0; i < a.length; i++) rv[i] = a[i];
	    for(int i = 0; i < b.length; i++) rv[i + a.length] = b[i];
	    return rv;
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
}
