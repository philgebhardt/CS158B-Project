package NESimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

import Crypto.*;
import Structure.*;
import Structure.Byte;

public class NEAgent extends Thread
{
	public static final int REQUEST_ARG = 0;
	public static final int OID_ARG = 1;
	public static final int VALUE_ARG = 2;
	public static final int TYPE_ARG = 3;
	
	public static int seed = 0;
	
	private TrapHandler trapHandler;
	private Lock lock;
	private OrderedTree<OID> data;
	private HashMap<String, User> users;
	ServerSocket serverSocket;
    Socket clientSocket;
	
	public NEAgent(OrderedTree<OID> data)
	{
	    this.lock = null;
		this.data = data;
		this.users = new HashMap<String, User>();
	}
	
	public void addUser(User user)
	{
	    users.put(user.getName(), user);
	}
	
	public void giveLock(Lock lock)
	{
	    this.lock = lock;
	}
	
	public Lock getLockObject()
	{
	    return lock;
	}
	
	@Override
	public void run()
	{
	    trapHandler = new TrapHandler();
        trapHandler.setDaemon(true);
        trapHandler.giveLock(lock);
        trapHandler.start();
	    while(true)
	    {
	        serverSocket = null;
	        clientSocket = null;
	        OutputStream out = null;
	        InputStream in = null;
	        
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
	            out = clientSocket.getOutputStream();
	            in = clientSocket.getInputStream();
	            
	            byte[] input, output, iv, message;
	            String name;
	            int totalBytes;
	            
	            input = new byte[1000];
	            totalBytes = in.read(input);
	            input = Byte.copy(input, 0, totalBytes);
	            name = new String(input);
	            name = name.substring(0, name.indexOf(' '));
	            
	            iv = Byte.copy(input, name.length()+1, 16);
	            message = Byte.copy(input, name.length() + 16 + 1);
	            Key key = users.get(name).getKey();
	            
	            message = Crypto.AESCBCdecrypt(message, key, iv);
	            System.out.println(new String(message));
	            String processed = process(new String(message), users.get(name));
	            System.out.println(processed);
	            iv = Crypto.generateIV(0, 16);
	            message = Crypto.AESCBCencrypt(processed.getBytes(), key, iv);
	            output = Byte.concat(iv,message);
	            
	            for(int i = 0; i < output.length; i++) System.out.format("%d ", output[i]);
	            System.out.print("\n");
	            out.write(output);
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
	
	private String process(String input, User user)
	{
	    StringTokenizer st;
	    String outputLn;
	    st = new StringTokenizer(input, " ");
        int n = st.countTokens();
        if(n < OID_ARG+1)
        {
            outputLn = "Invalid request: expected [get/set/walk/trap] community_string oid {value}";
        }
        else
        {
            String[] args = new String[n];
            OID oid;
            for(int i = 0; i < n; i++) args[i] = st.nextToken();

            //Find OID(s)
            oid = data.get(args[OID_ARG]);
            //Determine request type
            switch(args[0])
            {
                case "get":
                    if(oid != null)
                        outputLn = oid.toString();
                    else
                        outputLn = "OID " + args[OID_ARG] + " not found.";
                    break;
                case "set":
                    if(n < VALUE_ARG+1)
                    {
                        outputLn = "set unsuccessful: No value specified.";
                    }
                    else
                    {
                        if(oid != null)
                        {
                            String s = "";
                            for(int i = VALUE_ARG; i < n; i++) s += args[i] + " ";
                            oid.setValue(s.trim());
                            outputLn = "set successful";
                        }
                        else outputLn = "set unsuccessful: OID " + args[OID_ARG] + " not found.";
                    }
                    break;
                case "walk":
                    outputLn = walk(data.seek(args[OID_ARG]));
                    break;
                case "trap":
                    if(n < TYPE_ARG+1)
                    {
                        outputLn = "trap command unsuccessful: No type specified.";
                    }
                    else
                    {
                        if(oid != null)
                        {
                            String value = "";
                            String host = clientSocket.getInetAddress().toString().substring(1);
                            for(int i = TYPE_ARG; i < n; i++) value += args[i] + " ";
                            trapHandler.addTrap(oid, TrapHandler.trapType(args[VALUE_ARG]), value.trim(), host, user);
                            outputLn = "trap set successful";
                        }
                        else outputLn = "trap command unsuccessful: OID " + args[OID_ARG] + " not found.";
                    }
                    break;
                default:
                    outputLn = "How did you get here?";
                    break;
            }
        }
        return outputLn;
	}
	
	private String walk(OrderedTree<OID> parent)
	{
	    if(parent.getRootData().isLeaf())
	    {
	        return parent.getRootData().toString() + "|";
	    }
	    else
	    {
	        String children = "";
	        ListIterator<OrderedTree<OID>> iter = parent.listIterator();
	        while(iter.hasNext())
	        {
	            children += walk(iter.next());
	        }
	        return children;
	    }
	}
}