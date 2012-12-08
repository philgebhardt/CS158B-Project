package NESimulator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	            
	            //INPUT FORMAT: username iv E("[get/set/walk/trap] OID {value}", userKey)
	            if( (inputLn = in.readLine()) != null)
	            {
	                User user;
	                int delim = inputLn.indexOf(' '); 
	                String userString = inputLn.substring(0, delim);
	                //Authenticate user
	                if( (user = authenticate(userString)) != null)
	                {
	                    delim++;
	                    byte[] iv = inputLn.substring(delim, delim+16).getBytes();
	                    String message = inputLn.substring(delim+16);
	                    try
	                    {
	                        message = Crypto.AESCBCdecrypt(message, user.getKey(), iv);
	                    }
	                    catch(Exception e)
	                    {
	                        System.err.format("Encryption failed: %s%n", e.getMessage());
	                        System.exit(-1);
	                    }
	                    
	                    while(lock.tryLock() == false);
	                    outputLn = process(message);
	                    lock.unlock();
	                    iv = Crypto.generateIV(seed++, 16);
	                    
	                    try
	                    {
	                        outputLn = "" + iv + Crypto.AESCBCencrypt(outputLn, user.getKey(), iv);
	                    }
	                    catch(Exception e)
                        {
                            System.err.format("Decryption failed: %s%n", e.getMessage());
                            System.exit(-1);
                        }
	                }
	                else
	                {
	                    outputLn = "Failed to authenticate user: " + userString;
	                    out.println(outputLn);
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
                    if(oid != null) outputLn = oid.toString();
                    else outputLn = "OID " + args[OID_ARG] + " not found.";
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
}
