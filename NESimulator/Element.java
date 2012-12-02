import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

public class Element extends Thread
{
    LinkedList<OrderedTree<String>> data;
    
    HashMap<String, String> tcpTable;
    LinkedList<String> systemInfo;
    
    static final Random random = new Random();
    
    public Element()
    {
        this.data = new LinkedList<OrderedTree<String>>();
    }
    public Element(LinkedList<OrderedTree<String>> data)
    {
        this.data = data;
    }
    public OrderedTree<String> agentData()
    {
        return new OrderedTree<String>("Root", data);
    }
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			doSomethingRandom();
		}
		
	}
	static void doSomethingRandom()
	{
	    
	}
}
