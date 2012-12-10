package RMON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

import Crypto.Crypto;
import NESimulator.TrapHandler;
import Structure.Byte;
import Structure.User;

public class ClientSideComm extends Thread
{
    HashMap<String, Alarm> alarms;
    HashMap<String, User> users;
    ServerSocket serverSocket;
    Socket clientSocket;
    InputStream in;
    OutputStream out;
    
    public ClientSideComm(HashMap<String, User> users, HashMap<String, Alarm> alarms)
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
                serverSocket = new ServerSocket(4445);
                clientSocket = serverSocket.accept();
                in = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();
                
                Socket agentSocket = null;
                InputStream inAgent = null;
                OutputStream outAgent = null;
                
                byte[] input, output, iv;
                String name, destIp, srcIp, processed;
                srcIp = clientSocket.getInetAddress().toString().substring(1);
                input = new byte[1000];
                int totalBytes = in.read(input);
                name = new String(input);
                name = name.substring(0, name.indexOf(' '));
                
                iv = Byte.copy(input, name.length() + 1, 16);
                destIp = new String(Byte.copy(input, name.length() + 16 + 1 + 1));
                destIp = destIp.substring(0, destIp.indexOf(' '));
                
                input = Byte.copy(input, name.length() + 18 + destIp.length() + 1, totalBytes - (name.length() + 18 + destIp.length() + 1));
                input = Crypto.AESCBCdecrypt(input, users.get(name).getKey(), iv);

                boolean is_trap = proccess(new String(input), users.get(name), srcIp);
                
                iv = Crypto.generateIV(0, 16);
                output = Crypto.AESCBCencrypt(input, users.get("RMON").getKey(), iv);
                output = Byte.concat("RMON ".getBytes(), Byte.concat(iv, output));
                
                agentSocket = new Socket(destIp, 4447);
                inAgent = agentSocket.getInputStream();
                outAgent = agentSocket.getOutputStream();
                outAgent.write(output);
                input = new byte[1000];
                totalBytes = inAgent.read(input);
                
                agentSocket.close();
                inAgent.close();
                outAgent.close();
                
                if(is_trap)
                {
                    //Do not forward response to client
                }
                else
                {
                    iv = Byte.copy(input, 0, 16);
                    output = Byte.copy(input, 16, totalBytes - 16);
                    output = Crypto.AESCBCdecrypt(output, users.get("RMON").getKey(), iv);
                    output = Crypto.AESCBCencrypt(output, users.get(name).getKey(), iv);
                    output = Byte.concat(iv, output);
                    out.write(output);
                }
                
                serverSocket.close();
                clientSocket.close();
                in.close();
                out.close();
                
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | ShortBufferException | IllegalBlockSizeException | BadPaddingException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private boolean proccess(String input, User user, String ipForward)
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
                Alarm alarm = new Alarm(user, ipForward, name, value, TrapHandler.trapType(type));
                //Add alarm to alarm list
                alarms.put(alarm.getName(), alarm);
                return true;
            default:
                //forward message
                break;
        }
        return false;
    }
}
