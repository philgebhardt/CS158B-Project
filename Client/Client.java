import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class Client extends JFrame{
	
	char newline = '\n';
	
	JPanel north1;
	JPanel north2;
    JPanel north;
	JPanel west;
    JPanel center;
    JPanel south;
    JPanel east;
    JPanel treeDisplay;
    
    JLabel agentAddress;
    JLabel rmonAddress;
    JLabel commString;
    JLabel oid;
    JLabel textLog;
    JLabel mibTree;
    JLabel userLabel;
	JLabel passwordLabel;
	
	JTextField userInput;
    JTextField oidInput;
    JTextField agentInput;
    JTextField rmonInput;
    JTextField commInput;
    
    JPasswordField passwordInput;
    
    JTextArea log;
    
    JButton get; 
    JButton set; 
    JButton walk; 
    JButton trap; 
    JButton displayButton;
    
    String userString;
    String passwordString;
    String rmonString;
    String agentString;
    String communityString;
    String oidString;
    
    ArrayList<TreeNode> list;
	
	public Client(String title)
	{
		super(title);
		setFrame();
	}
	public void setFrame()
	{	
		rmonString = "";
	    agentString = "";
	    communityString = "";
	    oidString = "";
		
	    north1 = new JPanel();
	    north2 = new JPanel();
	    north = new JPanel();
	    west = new JPanel();
	    center = new JPanel();
	    south = new JPanel();
	    east = new JPanel();
	    

	    userLabel = new JLabel("User Name: ");
	    passwordLabel = new JLabel("Password: ");
	    agentAddress = new JLabel("Agent");
	    rmonAddress = new JLabel("RMON");
	    commString = new JLabel("Community String");
	    oid = new JLabel("OID");
	    textLog = new JLabel("LOG");
	    mibTree = new JLabel("MIB Tree");
	    

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
	    commInput = new JTextField();
	    commInput.setPreferredSize(new Dimension(145, 25));
	   
	    log = new JTextArea();
	    log.setEditable(false);
	    treeDisplay = new JPanel();
	    treeDisplay.setPreferredSize(new Dimension(160, 555));
	    treeDisplay.setLayout(new BoxLayout(treeDisplay, BoxLayout.Y_AXIS));
	    
	    list = new ArrayList<TreeNode>();
	    list.add(new TreeNode("- tcpConnTable", "0"));
	    list.add(new TreeNode("   - tcpConnEntry", "0.0"));
	    list.add(new TreeNode("      - tcpConnState", "0.0.0"));
	    list.add(new TreeNode("      - tcpConnLocalAddress", "0.0.1"));
	    list.add(new TreeNode("      - tcpConnLocalPort", "0.0.2"));
	    list.add(new TreeNode("      - tcpConnRemAddress", "0.0.3"));
	    list.add(new TreeNode("      - tcpConnRemPort", "0.0.4"));
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
	    
	    
	    
	    get = new JButton("GET");
	    get.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	userString = userInput.getText();
            	passwordString = passwordInput.getPassword().toString();
            	try
            	{
            		MessageDigest m = MessageDigest.getInstance("MD5");
            		m.reset();
            		m.update(passwordString.getBytes(), 0, passwordString.length());
            		byte messageDigest[] = m.digest();
            		StringBuffer hexString = new StringBuffer();
            		for(int i = 0; i < messageDigest.length;i++)
            			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            		updateLog(passwordString + ": " + hexString.toString());
            	}
            	catch(NoSuchAlgorithmException n)
            	{
            	}
            	
            	
            	rmonString = rmonInput.getText();
        	    agentString = agentInput.getText();
        	    communityString = commInput.getText();
        	    oidString = oidInput.getText();
                updateLog("get " + oidInput.getText() + "...");
                if (rmonString.isEmpty() == false && agentString.isEmpty())
                	updateLog(communicate("get " + communityString + " " + oidString, rmonString));
                else if (rmonString.isEmpty() && agentString.isEmpty() == false)
                	updateLog(communicate("get " + communityString + " " + oidString, agentString));
                done();
            }
        }); 
	    set = new JButton("SET");
	    set.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            {
            	rmonString = rmonInput.getText();
        	    agentString = agentInput.getText();
        	    communityString = commInput.getText();
        	    oidString = oidInput.getText();
            	updateLog("set " + oidInput.getText() + "...");

            	 if (rmonString.isEmpty() == false && agentString.isEmpty())
            		 updateLog(communicate("set " + communityString + " " + oidString, rmonString));
                 else if (rmonString.isEmpty() && agentString.isEmpty() == false)
                   	 updateLog(communicate("set " + communityString + " " + oidString, agentString));
            	done();
            }
        }); 
	    walk = new JButton("WALK");
	    walk.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            {
            	rmonString = rmonInput.getText();
        	    agentString = agentInput.getText();
        	    communityString = commInput.getText();
        	    oidString = oidInput.getText();
            	updateLog("walk " + oidInput.getText() + "...");

            	 if (rmonString.isEmpty() == false && agentString.isEmpty())
            		 updateLog(communicate("walk " + communityString + " " + oidString, rmonString));
                 else if (rmonString.isEmpty() && agentString.isEmpty() == false)
                 	 updateLog(communicate("walk " + communityString + " " + oidString, agentString));
            	done();
            }
        }); 
	    trap = new JButton("TRAP");
	    trap.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            { 
            	rmonString = rmonInput.getText();
        	    agentString = agentInput.getText();
        	    communityString = commInput.getText();
        	    oidString = oidInput.getText();
            	updateLog("trap " + oidInput.getText() + "...");

            	 if (rmonString.isEmpty() == false && agentString.isEmpty())
            		 updateLog(communicate("trap " + communityString + " " + oidString, rmonString));
                 else if (rmonString.isEmpty() && agentString.isEmpty() == false)
                 	 updateLog(communicate("trap " + communityString + " " + oidString, agentString));
            	done();
            }
        }); 

	    west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
	    west.setBackground(Color.GRAY);
	    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
	    center.setBackground(Color.GRAY);
		north2.setLayout(new FlowLayout());
	    north2.setBackground(Color.GRAY);
	    north1.setLayout(new FlowLayout());
	    north1.setBackground(Color.GRAY);
	    north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
	    north.setBackground(Color.GRAY);
	    south.setLayout(new FlowLayout());
	    south.setBackground(Color.GRAY);
	    east.setBackground(Color.GRAY);

		north1.add(userLabel);
		north1.add(userInput);
		north1.add(passwordLabel);
		north1.add(passwordInput);
	    north2.add(rmonAddress);
	    north2.add(rmonInput);
	    north2.add(agentAddress);
	    north2.add(agentInput);
	    north2.add(commString);
	    north2.add(commInput);
	    north2.add(oid);
	    north2.add(oidInput);
	    north.add(north1);
	    north.add(north2);
	    
	    west.setPreferredSize(new Dimension(160, 555));
	    west.add(mibTree);
	    west.add(treeDisplay);
	    
	    center.add(textLog);
	    center.add(log);
	    
	    south.add(get);
	    south.add(set);
	    south.add(walk);
	    south.add(trap);
	    
	    add(north, BorderLayout.NORTH);
	    add(west, BorderLayout.WEST);
	    add(center, BorderLayout.CENTER);
	    add(south, BorderLayout.SOUTH);
	    add(east, BorderLayout.EAST);
	    
	    setSize(850, 700);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	private void updateLog(String toAppend)
	{
		log.append(toAppend + newline);
	}
	
	private void done()
	{
		updateLog("done!" + newline);
		
	}
	private String communicate(String command, String ipString)
	{
		Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String report = "Error";
        try
        {
        	
            echoSocket = new Socket(ipString, 4444);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		    out.println(command);
		    report = in.readLine();
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
}
