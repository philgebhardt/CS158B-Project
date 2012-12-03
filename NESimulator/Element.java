import java.util.LinkedList;
import java.util.Random;

public class Element extends Thread
{    
    OrderedTree<OID> tcpConnTable;
    LinkedList<OID> systemInfo;
    
    static final Random random = new Random();
    
    public Element()
    {
        this.tcpConnTable = new OrderedTree<OID>(new OID("TCP_CONN_TABLE", null), new LinkedList<OrderedTree<OID>>());
        this.systemInfo = new LinkedList<OID>();
    }
    public OrderedTree<OID> agentData()
    {

        return tcpConnTable;
    }
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			doSomethingRandom();
		}
		
	}
	void doSomethingRandom()
	{
	    int p;
	    //Probabilistic Insertion
	    p = random.nextInt(100);
	    if(p < 50)
	    {
	        OID newOID = createRandomTCP_OID();
	        tcpConnTable.listIterator().add(new OrderedTree<OID>(newOID, null));
	    }
	}
	static OID createRandomTCP_OID()
	{
	    int q1,q2,q3,q4,p;
	    String name = "";
	    
	    q1 = random.nextInt(255);
	    q2 = random.nextInt(255);
	    q3 = random.nextInt(255);
	    q4 = random.nextInt(255);
	    p = random.nextInt(1000);
	    name += q1 + "." + q2 + "." + q3 + "." + q4 + "." + p + ".";
	    
	    q1 = random.nextInt(255);
        q2 = random.nextInt(255);
        q3 = random.nextInt(255);
        q4 = random.nextInt(255);
        p = random.nextInt(1000);
        name += q1 + "." + q2 + "." + q3 + "." + q4 + "." + p;
        
        return new OID(name, "ESTABLISHED");
	}
}
