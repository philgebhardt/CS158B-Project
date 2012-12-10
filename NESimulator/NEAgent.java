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

public class NEAgent extends Thread
{
	public static final int REQUEST_ARG = 0;
	public static final int OID_ARG = 1;
	public static final int VALUE_ARG = 2;
	
	public static int seed = 0;
	
	private Lock lock;
	private OrderedTree<OID> data;
	private HashMap<String, User> users;
	
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
	    while(true)
	    {
	        ServerSocket serverSocket = null;
	        Socket clientSocket = null;
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
	            input = copy(input, 0, totalBytes);
	            name = new String(input);
	            name = name.substring(0, name.indexOf(' '));
	            
	            iv = copy(input, name.length()+1, 16);
	            message = copy(input, name.length() + 16 + 1);
	            Key key = users.get(name).getKey();
	            
	            message = Crypto.AESCBCdecrypt(message, key, iv);
	            String processed = process(new String(message));
	            System.out.println(processed);
	            iv = Crypto.generateIV(0, 16);
	            message = Crypto.AESCBCencrypt(processed.getBytes(), key, iv);
	            output = concat(iv,message);
	            
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
	
	private String process(String input)
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
                        if(oid != null)
                        {
                            oid.setValue(args[VALUE_ARG]);
                            outputLn = "set successful";
                        }
                        else outputLn = "set unsuccessful: OID " + args[OID_ARG] + " not found.";
                    }
                    else
                    {
                        outputLn = "set unsuccessful: No value specified.";
                    }
                    break;
                case "walk":
                    outputLn = walk(data.seek(args[OID_ARG]));
                    break;
                case "trap":
                    outputLn = "Under Construction";
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
	
	private static byte[] concat(byte[] a, byte[] b)
	{
	    byte[] rv = new byte[a.length + b.length];
	    for(int i = 0; i < a.length; i++) rv[i] = a[i];
	    for(int i = 0; i < b.length; i++) rv[i + a.length] = b[i];
	    return rv;
	}
	
	private static byte[] copy(byte[] a, int offset, int len)
	{
	    byte[] rv = new byte[len];
	    for(int i = 0; i < len; i++) rv[i] = a[offset + i];
	    return rv;
	}
	
	private static byte[] copy(byte[] a, int offset)
    {
        byte[] rv = new byte[a.length - offset];
        for(int i = 0; i < a.length - offset; i++) rv[i] = a[offset + i];
        return rv;
    }
	
	/**
	private User authenticate(String username)
	{
	    if(users.containsKey(username))
	    {
	        return users.get(username);
	    }
	    else
	    {
	        return null;
	    }
	}
	**/
}
