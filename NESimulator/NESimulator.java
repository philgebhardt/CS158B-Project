import java.util.Scanner;


public class NESimulator
{
	public static void main(String args[])
	{
		NEAgent agent;
		Element element;
		Scanner in;
		String line;
		
		//Initialize threads
		element = new Element();
		agent = new NEAgent(element.agentData());
		
		
		//Run as background threads
		agent.setDaemon(true);
		element.setDaemon(true);
		
		//Start background threads
		element.start();
		agent.start();
		
		//Run simulator
		in = new Scanner(System.in);
		line = null;
		while( !(line = in.nextLine()).equals("q") )
		{
		    //Do nothing
		}
	}
}
