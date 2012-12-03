import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class Element extends Thread
{

    static String[] TCP_OPEN_CONN_STATES =
    { "SYN_SEND", "SYN_RECEIVED", "ESTABLISHED", "LISTEN" };
    static String[] TCP_CLOSE_CONN_STATES =
    { "FIN_WAIT_1", "TIMED_WAIT", "CLOSE_WAIT", "FIN_WAIT_2", "LAST_ACK",
            "CLOSED" };

    OrderedTree<OID> tcpConnTable;
    LinkedList<OID> systemInfo;

    static final Random random = new Random();

    public Element()
    {
        this.tcpConnTable = new OrderedTree<OID>(
                new OID("TCP_CONN_TABLE", null),
                new LinkedList<OrderedTree<OID>>());
        this.systemInfo = new LinkedList<OID>();
    }

    public OrderedTree<OID> agentData()
    {

        return tcpConnTable;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            probabilisticInsertion();
            probabilisticRemoval();
            probabilisticModification();
        }

    }

    void probabilisticInsertion()
    {
        int p;
        // Probabilistic Insertion
        p = random.nextInt(100);
        if (p < 25)
        {
            OID newOID = createRandomTCP_OID();
            tcpConnTable.listIterator().add(new OrderedTree<OID>(newOID, null));
        }
    }

    void probabilisticRemoval()
    {
        int p, r;
        // Probabilistic Removal
        p = random.nextInt(100);
        if (p < 50)
        {
            ListIterator<OrderedTree<OID>> iter = tcpConnTable.listIterator();
            r = random.nextInt(30);
            while (iter.hasNext())
            {
                if (iter.nextIndex() == r)
                {
                    iter.next();
                    iter.remove();
                    break;
                }
                else
                {
                    iter.next();
                }
            }
        }
    }

    void probabilisticModification()
	{
	    int p, r, n;
	    OID oid;
	    p = random.nextInt(100);
	    if(p < 75)
	    {
	        ListIterator<OrderedTree<OID>> iter = tcpConnTable.listIterator();
	        n = 0;
	        while(iter.hasNext())
	        {
	            iter.next();
	            n++;
	        }
	        r = n - random.nextInt(n+1);
	        while(iter.hasPrevious() && r > 1)
	        {
	            iter.previous();
	            r--;
	        }
	        
	        try
	        {
	            oid = iter.previous().getRootData();
	            switch(oid.getValue())
	            {
	                //SYN_SEND -> SYN_RECEIVED
	                case "SYN_SEND":
	                    oid.setValue("SYN_RECEIVED");
	                    break;
	                //SYN_RECEIVED -> ESTABLISHED
	                case "SYN_RECEIVED":
	                    oid.setValue("ESTABLISHED");
	                    break;
	                //LISTEN -> SYN_RECEIVED or ESTABLISHED
	                case "LISTEN":
	                    r = random.nextInt(2) + 1;
	                    oid.setValue(TCP_OPEN_CONN_STATES[r]);
	                    break;
	                //ESTABLISHED -> FIN_WAIT
	                case "ESTABLISHED":
	                    oid.setValue("FIN_WAIT");
	                    break;
	                //FIN_WAIT -> CLOSED
	                case "FIN_WAIT":
	                    oid.setValue("CLOSED");
	                    break;
	            }
	        }
	        catch(NoSuchElementException e)
	        {
	            System.out.println("[ELEMENT] No element error.");
	            return;
	        }
	    }
	}

    static OID createRandomTCP_OID()
    {
        int q1, q2, q3, q4, p, r;
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

        r = random.nextInt(TCP_OPEN_CONN_STATES.length);
        return new OID(name, TCP_OPEN_CONN_STATES[r]);
    }
}
