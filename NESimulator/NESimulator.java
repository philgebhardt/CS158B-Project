package NESimulator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

import Crypto.Key;
import Structure.*;

/**
 * This is the supervising class of the Element and
 * NEAgent classes. The NESimulator is responsible
 * for initializing configuration of both child
 * classes as well as process command line input
 * from a user.
 * 
 * @author Philip Gebhardt
 * @version Fall 2012, CS158B
 * */
public class NESimulator
{
    static NEAgent agent;
    static Element element;
    static LinkedList<User> users;
    
	public static void main(String args[])
	{
	    String line;
	    Scanner in;
	    StringTokenizer st;
	    
	    pullConfigInfo();
	    initializeThreads();
	    initializeUsers();
		startThreads();
		
		//Run simulator
		in = new Scanner(System.in);
		while( (line = in.nextLine()) != null )
		{
		    st = new StringTokenizer(line, " ");
		    if(!st.hasMoreTokens()) continue;
		    switch(st.nextToken())
		    {
    		    case "config":
    		        configureAgent();
    		        break;
    		    case "stat":
    		        agentStatistics();
    		        break;
    		    case "exit":
    		        return;
    	        default:
    	            System.out.println("Invalid command or argument(s).");
    	            break;
		    }
		}
	}
	
	/**
	 * This method initializes the two child classes Element
	 * and NEAgent. These classes are treated as threads and
	 * so they must be initialized with shared memory and a
	 * concurrency lock.
	 */
	public static void initializeThreads()
	{
	    //Initialize threads
        element = new Element(listSystemInfo());
        agent = new NEAgent(element.agentData());
        Lock myLock = new MyLock();
        element.giveLock(myLock);
        agent.giveLock(myLock);
        
        //Run as background threads
        agent.setDaemon(true);
        element.setDaemon(true);
	}
	
	public static void initializeUsers()
	{
	    for(User user : users)
	    {
	        agent.addUser(user);
	    }
	}
	
	public static void startThreads()
	{
	    //Start background threads
        element.start();
        agent.start();
	}
	
	/**
	 * This method searches for a configuration file
	 * that is used to load user and Crypto information.
	 * After this method is called, the users list should
	 * be filled with preloaded users.
	 */
	public static void pullConfigInfo()
	{
	    String line;
	    users = new LinkedList<User>();
	    FileSystem fs = FileSystems.getDefault();
	    Path configFile = fs.getPath("CONFIG.agent");
	    
	    try
	    {
	        InputStream in = Files.newInputStream(configFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        
	        line = null;
	        while(!(line = reader.readLine()).equals("USERS"));
	        while((line = reader.readLine()) != null)
	        {
	            users.add(createUser(line));
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
	
	/**
	 * Creates and returns a user with a given name.
	 * 
	 * @param input - name of the user to be created
	 * @return a User instance with specified name
	 */
	public static User createUser(String input)
	{
	    StringTokenizer st;
        String name;
        byte[] keyBytes = new byte[16];
        st = new StringTokenizer(input, " ");
        name = st.nextToken();
        for(int i = 0; i < 16; i++) keyBytes[i] = (byte) Integer.parseInt(st.nextToken());
        return new User(name, new Key(keyBytes));
	}
	
	public static void configureAgent()
	{
	    System.out.println("Config under construction.");
	}
	
	public static void agentStatistics()
	{
	    System.out.println("Agent stats under construction.");
	}
	
	/**
	 * Creates data structure for organizing the Element's system
	 * information. The data structure used and passed by this
	 * method is a LinkedList of type OrderedTree<OID>.
	 * @return list of system information
	 */
	public static LinkedList<OrderedTree<OID>> listSystemInfo()
	{
	    LinkedList<OrderedTree<OID>> list = new LinkedList<OrderedTree<OID>>();
	    list.add(new OrderedTree<OID>(new OID("SysName", "Router1"), null));
	    list.add(new OrderedTree<OID>(new OID("SysLocation", "On the dock of the bay..."), null));
	    list.add(new OrderedTree<OID>(new OID("SysContact", "(555)555-5555"), null));
	    list.add(new OrderedTree<OID>(new OID("SysUptime", "0"), null));
	    return list;
	}
}
