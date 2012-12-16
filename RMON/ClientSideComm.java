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
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import Crypto.Crypto;
import Crypto.Key;
import NESimulator.TrapHandler;
import Structure.Byte;
import Structure.User;

public class ClientSideComm extends Thread
{
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
    
    public ClientSideComm(HashMap<String, User> users, HashMap<String, Alarm> alarms)
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
                boolean is_trap;
                
                input = serveClient();
                
                while(lock.tryLock() == false);
                is_trap = proccess(new String(input));
                lock.unlock();
                
                if(is_trap) //Set trap on agent
                {
                    //Encrypt with RMON key and send to agent
                    //Do not forward agent response to client
                    byte[] response = agentInterface(input, users.get("RMON").getKey());
                    //TODO: doSomethingWithResponse(response);
                    System.out.println(new String(response));
                }
                else //Forward request
                {
                    //Encrypt with user key and sent to agent
                    //Forward response from agent to client
                    byte[] response = agentInterface(input, user.getKey());
                    clientOut.write(response);
                }
                Thread.sleep(2000);
                teardown();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private byte[] serveClient() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        byte[] input, iv;
        String name;
        int totalBytes, offset;
        
        serverSocket = new ServerSocket(4444);
        clientSocket = serverSocket.accept();
        clientIn = clientSocket.getInputStream();
        clientOut = clientSocket.getOutputStream();
        
        srcIp = clientSocket.getInetAddress().toString().substring(1);
        input = new byte[1000];
        totalBytes = clientIn.read(input);
                
        name = new String(input);
        name = name.substring(0, name.indexOf(' '));
        user = users.get(name);
        
        offset = name.length() + 1;
        iv = Byte.copy(input, offset, 16);
        
        offset += iv.length + 1;
        destIp = new String(Byte.copy(input, offset));
        destIp = destIp.substring(0, destIp.indexOf(' '));
        
        offset += destIp.length() + 1;
        input = Byte.copy(input, offset, totalBytes - offset);
        return Crypto.AESCBCdecrypt(input, user.getKey(), iv);
    }
    
    private boolean proccess(String input)
    {
        StringTokenizer st = new StringTokenizer(input, " ");
        int n = st.countTokens();
        String args[] = new String[n];
        int i = 0;
        while(st.hasMoreTokens()) args[i++] = st.nextToken();
        
        switch(args[0])
        {
            case "trap":
                //Read trap message contents
                String name, type, value;
                name = args[1];
                type = args[2];
                value = "";
                for(i = 3; i < n; i++) value += args[i];
                //Create alarm
                Alarm alarm = new Alarm(user, srcIp, name, value, TrapHandler.trapType(type));
                //Add alarm to alarm list
                alarms.put(alarm.getName(), alarm);
                return true;
            default:
                //forward message
                break;
        }
        return false;
    }
    
    private byte[] agentInterface(byte[] message, Key key) throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        int totalBytes;
        byte[] iv;
        byte[] input = new byte[1000];
        agentSocket = new Socket(destIp, 4444);
        agentIn = agentSocket.getInputStream();
        agentOut = agentSocket.getOutputStream();
        iv = Crypto.generateIV(0, 16);
        
        message = Crypto.AESCBCencrypt(message, key, iv);
        agentOut.write(message);
        totalBytes = agentIn.read(input);
        
        iv = Byte.copy(message, 0, 16);
        message = Byte.copy(message, 16, totalBytes-16);
        message = Crypto.AESCBCdecrypt(message, key, iv);
        
        //TODO: Logging goes here
        System.out.println(new String(message));
        
        return message;
    }
    
    private void teardown() throws IOException
    {
        serverSocket.close();
        clientSocket.close();
        agentSocket.close();
        clientIn.close();
        clientOut.close();
        agentIn.close();
        agentOut.close();
    }
}
