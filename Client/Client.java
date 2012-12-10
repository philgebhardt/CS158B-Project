package Client;

import Crypto.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Client extends JFrame{
	
	char newline = '\n';
	
	JPanel north1;
	JPanel north2;
	JPanel north3;
    JPanel north;
	JPanel west;
    JPanel center;
    JPanel south;
    JPanel east;
    JPanel treeDisplay;
    
    JLabel agentAddress;
    JLabel rmonAddress;
    JLabel oid;
    JLabel textLog;
    JLabel mibTree;
    JLabel userLabel;
	JLabel passwordLabel;
	JLabel extraCommands;
	
	JTextField userInput;
    JTextField oidInput;
    JTextField agentInput;
    JTextField rmonInput;
    JTextField commandInput;
    
    JPasswordField passwordInput;
    
    JTextArea log;
    JScrollPane scrollable;
    
    JButton get; 
    JButton set; 
    JButton walk; 
    JButton alarm; 
    JButton displayButton;
    
    String userString;
    String passwordString;
    String rmonString;
    String agentString;
    String oidString;
    String extraString;
    
    byte[] communicationString;
    
    Key key;
    
    ArrayList<TreeNode> list;
	
	public Client(String title)
	{
		super(title);
		threading();
		
	}
	
	private void threading()
	{
		Runnable r1 = new Runnable() {
			public void run() {
				clientInterface();
			}
		};
		Runnable r2 = new Runnable() {
			public void run() {
				while(true)
					alarmHandler();
			}
		};
		Thread thr1 = new Thread(r1);
		Thread thr2 = new Thread(r2);
		thr1.start();
		thr2.start();
	}
	
	private void clientInterface()
	{
		initializeStrings();
		initializePanels();
	    initializeLabels();
	    initializeInputFields();
	    initializeTextAreas();
	    initializeTreeTable();
	    setButtons();
	    setLayoutandBackgrounds();
	    combineEverything();
		setFrame();
	}
	private void alarmHandler()
	{
		ServerSocket serverSocket = null;
        Socket clientSocket = null;
        OutputStream out = null;
        InputStream in = null;
        
        try
        {
            serverSocket = new ServerSocket(4445);
            System.out.println("Server is listening...");
            clientSocket = serverSocket.accept();
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            
            byte[] input, iv, message;
            String name;
            int totalBytes;
            
            input = new byte[1000];
            totalBytes = in.read(input);
            input = copy(input, 0, totalBytes);
            name = new String(input);
            name = name.substring(0, name.indexOf(' '));
            
            iv = copy(input, 0, 16);
            message = copy(input, 16);
            
            message = Crypto.AESCBCdecrypt(message, key, iv);

            
            updateLog(new String(message));
        }
        catch(IOException e)
        {
            System.out.println("ServerSocket connection error.");
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
	private void setFrame()
	{	
	    setSize(850, 700);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	private void initializeStrings()
	{
		rmonString = "";
	    agentString = "";
	    oidString = "";
	}
	private void initializePanels()
	{
	    north1 = new JPanel();
	    north2 = new JPanel();
	    north3 = new JPanel();
	    north = new JPanel();
	    west = new JPanel();
	    center = new JPanel();
	    south = new JPanel();
	    east = new JPanel();;
	}
	private void initializeLabels()
	{
		userLabel = new JLabel("User Name: ");
	    passwordLabel = new JLabel("Password: ");
	    agentAddress = new JLabel("Agent");
	    rmonAddress = new JLabel("RMON");
	    oid = new JLabel("OID");
	    textLog = new JLabel("LOG");
	    mibTree = new JLabel("MIB Tree");
	    extraCommands = new JLabel("Extra Command");
	}
	private void initializeInputFields()
	{
		userInput = new JTextField();
	    userInput.setPreferredSize(new Dimension(145, 25));
	    passwordInput = new JPasswordField();
	    passwordInput.setPreferredSize(new Dimension(145, 25));
	    oidInput = new JTextField();
	    oidInput.setPreferredSize(new Dimension(145, 25));
	    agentInput = new JTextField();
	    agentInput.setPreferredSize(new Dimension(145, 25));
	    rmonInput = new JTextField();
	    rmonInput.setPreferredSize(new Dimension(145, 25));
	    commandInput = new JTextField();
	    commandInput.setPreferredSize(new Dimension(300, 25));
	}
	private void initializeTextAreas()
	{
		log = new JTextArea();
	    log.setEditable(false);
	    scrollable = new JScrollPane(log);
	    scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    
	    treeDisplay = new JPanel();
	    treeDisplay.setPreferredSize(new Dimension(160, 555));
	    treeDisplay.setLayout(new BoxLayout(treeDisplay, BoxLayout.Y_AXIS));
	}
	private void initializeTreeTable()
	{
		list = new ArrayList<TreeNode>();
		list.add(new TreeNode("* MIB-2", "0"));
	    list.add(new TreeNode("   * System", "0.0"));
	    list.add(new TreeNode("      * SysName", "0.0.0"));
	    list.add(new TreeNode("      * SysLocation", "0.0.1"));
	    list.add(new TreeNode("      * SysContact", "0.0.2"));
	    list.add(new TreeNode("      * SysUptime", "0.0.3"));
	    list.add(new TreeNode("   * TCP", "0.1"));
	    list.add(new TreeNode("      * TCP-ConnTable", "0.1.0"));
	    list.add(new TreeNode("      * CurrConnections", "0.1.1"));
	    for (int i = 0; i < list.size(); i++)
	    {
	    	final TreeNode nextNode = list.get(i);
	    	nextNode.getLabel().addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                oidInput.setText(nextNode.getOID());
	            }
	        });
	    	treeDisplay.add(nextNode.getLabel());
	    }
	}
	private void setButtons()
	{
		get = new JButton("GET");
	    get.addActionListener(new ActionListener() {
	    	 
	        public void actionPerformed(ActionEvent e)
	        {   		
	        	sendRequest("get");
	        }
	    }); 
	    set = new JButton("SET");
	    set.addActionListener(new ActionListener() {
	    	 
	        public void actionPerformed(ActionEvent e)
	        {
	        	sendRequest("set");
	        }
	    }); 
	    walk = new JButton("WALK");
	    walk.addActionListener(new ActionListener() {
	    	 
	        public void actionPerformed(ActionEvent e)
	        {
	        	sendRequest("walk");
	        }
	    }); 
	    alarm = new JButton("ALARM");
	    alarm.addActionListener(new ActionListener() {
	    	 
	        public void actionPerformed(ActionEvent e)
	        { 
	        	sendRequest("alarm");
	        }
	    });
	}
	private void setLayoutandBackgrounds()
	{
	    west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
	    west.setBackground(Color.GRAY);
	    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
	    center.setBackground(Color.GRAY);
	    north3.setLayout(new FlowLayout());
	    north3.setBackground(Color.GRAY);
		north2.setLayout(new FlowLayout());
	    north2.setBackground(Color.GRAY);
	    north1.setLayout(new FlowLayout());
	    north1.setBackground(Color.GRAY);
	    north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
	    north.setBackground(Color.GRAY);
	    south.setLayout(new FlowLayout());
	    south.setBackground(Color.GRAY);
	    east.setBackground(Color.GRAY);
	}
	private void combineEverything()
	{

		north1.add(userLabel);
		north1.add(userInput);
		north1.add(passwordLabel);
		north1.add(passwordInput);
	    north2.add(rmonAddress);
	    north2.add(rmonInput);
	    north2.add(agentAddress);
	    north2.add(agentInput);
	    north2.add(oid);
	    north2.add(oidInput);
	    north3.add(extraCommands);
	    north3.add(commandInput);
	    north.add(north1);
	    north.add(north2);
	    north.add(north3);
	    
	    west.setPreferredSize(new Dimension(160, 555));
	    west.add(mibTree);
	    west.add(treeDisplay);
	    
	    center.add(textLog);
	    center.add(scrollable);
	    
	    south.add(get);
	    south.add(set);
	    south.add(walk);
	    south.add(alarm);
	    
	    add(north, BorderLayout.NORTH);
	    add(west, BorderLayout.WEST);
	    add(center, BorderLayout.CENTER);
	    add(south, BorderLayout.SOUTH);
	    add(east, BorderLayout.EAST);
	}
	
	private void updateLog(String toAppend)
	{
		log.append(toAppend + newline);
	}
	
	private void done()
	{
		updateLog("done!" + newline);
		
	}
	
	private byte[] prepAuthentication(String name, byte[] iv)
	{
    		return concatBytes(concatBytes(name.getBytes(), " ".getBytes()), iv);
	}
	
	private byte[] prepMessage(String password, String message, byte[] iv)
	{
    	try
    	{
    		MessageDigest m = MessageDigest.getInstance("MD5");
    		byte[] input1 = password.getBytes("UTF-8");
    		byte[] hash1 = m.digest(input1);
    		key = new Key(hash1);
    		return Crypto.AESCBCencrypt(message.getBytes(), key, iv);
    	}
    	catch(Exception e)
		{
			System.err.format("Bad encryption: %s%n", e.getMessage());
			return null;
		}
	}
	
	private String communicate(byte[] command, String ipString)
	{
		Socket echoSocket = null;
        OutputStream out = null;
        InputStream in = null;
        String report = "Error";
        int totalBytes = -1;
        byte[] input = new byte[1000];
        updateLog("PING!");
        try
        {
        	
            echoSocket = new Socket(ipString, 4444);
            out = echoSocket.getOutputStream();
            in = echoSocket.getInputStream();
		    out.write(command);
		    totalBytes = in.read(input);
		    return decryptMessage(input, totalBytes);
        }
        catch (UnknownHostException e)
        {
            System.out.println("Don't know about host: " + echoSocket.getInetAddress());
        }
        catch (IOException e)
        {
            System.out.println("Couldn't get I/O for the connection to: " + ipString);
            
        }
        try
        {
		    out.close();
    		in.close();
    		echoSocket.close();
        }
        catch (IOException e)
        {
        	System.out.println("Couldn't get I/O for the connection to: " + command);
        }
        return report;
	}

	private void sendRequest(String command)
	{
		rmonString = rmonInput.getText();
	    agentString = agentInput.getText();
	    oidString = oidInput.getText();
	    extraString = commandInput.getText();
		byte[] iv = Crypto.generateIV(0, 16);
	    
		communicationString = prepAuthentication(userInput.getText(), iv);
		if(command.equals("get"))
			communicationString = concatBytes( communicationString, prepMessage(new String(passwordInput.getPassword()),
					"get " + oidString, iv));
		else if (command.equals("set"))
			communicationString = concatBytes( communicationString, prepMessage(new String(passwordInput.getPassword()),
					"set " + oidString + " " + extraString, iv));
		else if (command.equals("walk"))
			communicationString = concatBytes( communicationString, prepMessage(new String(passwordInput.getPassword()),
					"walk " + oidString, iv));
		else
			communicationString = concatBytes( communicationString, prepMessage(new String(passwordInput.getPassword()),
					"trap " + oidString + " " + extraString, iv));
        
        if (rmonString.isEmpty() == false)
        	updateLog(communicate(communicationString, rmonString));
        else if (agentString.isEmpty() == false)
        	updateLog(communicate(communicationString, agentString));
        else
        	updateLog("Please enter destination address");
        done();
	}
	private byte[] concatBytes(byte[] a, byte[] b)
	{
		byte[] concat = new byte[a.length + b.length];
		int n = a.length;
		int m = b.length;
		for(int i = 0; i < n; i++)
			concat[i] = a[i];
		for(int i = 0; i < m; i++)
			concat[i + a.length] = b[i];
		
		return concat;
	}
	
	
	private String decryptMessage(byte[] input, int totalBytes)
	{
		//test
		for(int i = 0 ; i < totalBytes; i++)
		{
			updateLog(String.format("%d", input[i]));
		}
		//test end
		
		byte[] message = new byte[totalBytes - 16];
		byte[] iv = new byte[16];
		for ( int i = 0; i < 16; i++)
		{
			iv[i] = input[i];
		}
		int messageI = 16;
		for ( int i = 0; i < totalBytes - 16; i++, messageI++)
		{
			message[i] = input[messageI];
		}
		try
		{
			message = Crypto.AESCBCdecrypt(message, key, iv);
		}
		catch(Exception e)
		{
			updateLog("error decrypting message: " + e.getMessage());
		}
		return newLineAdder(new String (message));	
	}
	
	private String newLineAdder(String origMessage)
	{
		String modifiedMessage;
		
		modifiedMessage = origMessage.replace('|', '\n');
		
		return modifiedMessage;
	}
}
