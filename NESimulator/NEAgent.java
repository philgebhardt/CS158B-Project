import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;


public class NEAgent extends Thread
{
	public static final int REQUEST_ARG = 0;
	public static final int COMMUNITY_ARG = 1;
	public static final int OID_ARG = 2;
	public static final int VALUE_ARG = 3;
	
	private Lock lock;
	private OrderedTree<OID> data;
	private LinkedList<User> users;
	
	public NEAgent(OrderedTree<OID> data)
	{
	    this.lock = null;
		this.data = data;
		this.users = new LinkedList<User>();
	}
	
	public void addUser(User user)
	{
	    users.add(user);
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
	            
	            //Input format [get/set/walk/trap] community_string OID
	            if( (inputLn = in.readLine()) != null)
	            {
	                while(lock.tryLock() == false);
	                outputLn = process(inputLn);
	                lock.unlock();
	                out.println(outputLn);
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
            
            //Authenticate
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
	
}
