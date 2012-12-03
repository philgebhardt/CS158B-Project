import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class Client extends JFrame{
	
	char newline = '\n';
	
	JPanel west;
    JPanel north;
    JPanel center;
    JPanel south;
    
    JLabel agentAddress;
    JLabel rmonAddress;
    JLabel commString;
    JLabel oid;
    JLabel textLog;
    JLabel mibTree;
    
    JTextField oidInput;
    JTextField agentInput;
    JTextField rmonInput;
    JTextField commInput;
    
    JTextArea log;
    JTextArea treeDisplay;
    
    JButton get; 
    JButton set; 
    JButton walk; 
    JButton trap; 
    JButton displayButton;
	
	public Client(String title)
	{
		super(title);
		setFrame();
	}
	public void setFrame()
	{	
	    west = new JPanel();
	    north = new JPanel();
	    center = new JPanel();
	    south = new JPanel();
	    
	    agentAddress = new JLabel("Agent");
	    rmonAddress = new JLabel("RMON");
	    commString = new JLabel("Community String");
	    oid = new JLabel("OID");
	    textLog = new JLabel("LOG");
	    mibTree = new JLabel("MIB Tree");
	    
	    oidInput = new JTextField();
	    oidInput.setPreferredSize(new Dimension(300, 25));
	    agentInput = new JTextField();
	    agentInput.setPreferredSize(new Dimension(300, 25));
	    rmonInput = new JTextField();
	    rmonInput.setPreferredSize(new Dimension(300, 25));
	    commInput = new JTextField();
	    commInput.setPreferredSize(new Dimension(300, 25));
	   
	    log = new JTextArea();
	    log.setPreferredSize(new Dimension(900, 550));
	    log.setEditable(false);
	    treeDisplay = new JTextArea();
	    treeDisplay.setPreferredSize(new Dimension(200, 550));
	    treeDisplay.setEditable(false);
	    
	    get = new JButton("GET");
	    get.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                updateLog("get " + oidInput.getText() + "...");
                done();
            }
        }); 
	    set = new JButton("SET");
	    set.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            {
            	updateLog("set " + oidInput.getText() + "...");
            	done();
            }
        }); 
	    walk = new JButton("WALK");
	    walk.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            {
            	updateLog("walk " + oidInput.getText() + "...");
            	done();
            }
        }); 
	    trap = new JButton("TRAP");
	    trap.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            { 
            	updateLog("trap " + oidInput.getText() + "...");
            	done();
            }
        }); 
	    displayButton = new JButton("Display Tree");
	    displayButton.addActionListener(new ActionListener() {
	    	 
            public void actionPerformed(ActionEvent e)
            {
                updateLog("Retrieving MIB Tree");
                updateLog("RMON address : " + rmonInput.getText());
                updateLog("agent address : " + agentInput.getText());
                updateLog("community string : " + commInput.getText());  
                done();
            }
        }); 
	    
	    west.setLayout(new FlowLayout()); 
	    west.setBackground(Color.GRAY);
	    center.setLayout(new FlowLayout()); 
	    center.setBackground(Color.GRAY);
	    north.setLayout(new FlowLayout());
	    north.setBackground(Color.GRAY);
	    south.setLayout(new FlowLayout());
	    south.setBackground(Color.GRAY);
	    
	    north.add(rmonAddress);
	    north.add(rmonInput);
	    north.add(agentAddress);
	    north.add(agentInput);
	    north.add(commString);
	    north.add(commInput);
	    north.add(displayButton);
	    
	    west.add(mibTree);
	    west.add(treeDisplay);
	    
	    center.add(textLog);
	    center.add(log);
	    
	    south.add(oid);
	    south.add(oidInput);
	    south.add(get);
	    south.add(set);
	    south.add(walk);
	    south.add(trap);
	    
	    add(north, BorderLayout.NORTH);
	    add(west, BorderLayout.WEST);
	    add(center, BorderLayout.CENTER);
	    add(south, BorderLayout.SOUTH);
	    
	    setSize(1240, 670);
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
}
