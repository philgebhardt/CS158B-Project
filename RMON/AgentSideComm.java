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
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import Crypto.*;
import Structure.Byte;
import Structure.User;

public class AgentSideComm extends Thread
{
    private static final int TIME_ARG = 0;
    private static final int NAME_ARG = 1;
    //private static final int TYPE_ARG = 2;
    //private static final int VALUE_ARG = 3;
    //private static final int OID_ARG = 4;
    
    HashMap<String, Alarm> alarms;
    HashMap<String, User> users;
    
    ServerSocket serverSocket;
    Socket clientSocket;
    Socket agentSocket;
    
    InputStream clientIn;
    InputStream agentIn;
    OutputStream clientOut;
    OutputStream agentOut;
    
    Lock lock;
    
    String srcIp, destIp;
    
    User user;
    
    public AgentSideComm(HashMap<String, User> users, HashMap<String, Alarm> alarms)
    {
        this.alarms = alarms;
        this.users = users;
    }
    
    public void giveLock(Lock l)
    {
        lock = l;
    }
    
    public void run()
    {
    	while(true)
    	{
	        try
	        {
	           byte[] input;
	           String response;
	           
	           input = serveAgent();
	           
	           while(lock.tryLock() == false);
	           response = process(new String(input));
	           lock.unlock();
	           
	           notifyClient(response);
	           tearDown();
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
    	}
    }
    
    private void tearDown() throws IOException
    {
        serverSocket.close();
        agentSocket.close();
        clientSocket.close();
        agentIn.close();
        agentOut.close();
        clientIn.close();
        clientOut.close();
    }
    
    private byte[] serveAgent() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        int totalBytes;
        byte[] input, iv;
        serverSocket = new ServerSocket(4445);
        agentSocket = serverSocket.accept();
        agentIn = agentSocket.getInputStream();
        agentOut = agentSocket.getOutputStream();
        
        input = new byte[1000];
        totalBytes = agentIn.read(input);
        iv = Byte.copy(input, 0, 16);
        input = Byte.copy(input, 16, totalBytes);
        input = Crypto.AESCBCdecrypt(input, users.get("RMON").getKey(), iv);
        return input;
    }
    
    private String process(String input)
    {
        int n, i;
        String message, args[];
        StringTokenizer st;
        
        st = new StringTokenizer(input, " ");
        n = st.countTokens();
        args = new String[n];
        
        i = 0;
        while(st.hasMoreTokens()) args[i] = st.nextToken();
        
        //message = "" + systemTime + ":" + name + ":" + type + ":" + value + ":" + oidString;
        message = handleAlarm(args[NAME_ARG], Long.parseLong(args[TIME_ARG]) );
        return message;
    }
    
    private void notifyClient(String message) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnknownHostException, IOException, InterruptedException
    {
        byte[] output, iv;
        iv = Crypto.generateIV(0, 16);
        output = Crypto.AESCBCencrypt(message.getBytes(), user.getKey(), iv);
        output = Byte.concat(iv, output);
        
        clientSocket = new Socket(destIp, 4445);
        clientIn = clientSocket.getInputStream();
        clientOut = clientSocket.getOutputStream();
        
        clientOut.write(output);
        Thread.sleep(2000);
    }
    
    private String handleAlarm(String name, long time)
    {
        Date date;
        Alarm alarm = alarms.get(name);
        destIp = alarm.getIpForward();
        user = alarm.getUser();
        
        if(alarm.has_crossed())
        {
            alarm.threshold_rescinded();
            
        }
        else
        {
            alarm.threshold_crossed();
        }
        date = new Date(time);
        return date.toString() + ": " + alarm.toString();
    }
}
