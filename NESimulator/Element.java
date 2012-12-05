import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class Element extends Thread
{
    static final long REFRESH_RATE = 200; //200 milliseconds
    static String[] TCP_OPEN_CONN_STATES =
    { "SYN_SEND", "SYN_RECEIVED", "ESTABLISHED", "LISTEN" };
    static String[] TCP_CLOSE_CONN_STATES =
    { "FIN_WAIT_1", "TIMED_WAIT", "CLOSE_WAIT", "FIN_WAIT_2", "LAST_ACK",
            "CLOSED" };

    OrderedTree<OID> tcpConnTable;
    OrderedTree<OID> systemInfo;
    Lock lock;
    
    static final Random random = new Random();

    public Element()
    {
        this.tcpConnTable = new OrderedTree<OID>(new OID("TCP_CONN_TABLE", null), new LinkedList<OrderedTree<OID>>());
        this.systemInfo = new OrderedTree<OID>(new OID("SYSTEM", null), new LinkedList<OrderedTree<OID>>());
    }
    
    public void giveLock(Lock lock)
    {
        this.lock = lock;
    }
    
    public Lock getLockObject()
    {
        return lock;
    }

    public OrderedTree<OID> agentData()
    {
        LinkedList<OrderedTree<OID>> list = new LinkedList<OrderedTree<OID>>();
        list.add(systemInfo);
        list.add(tcpConnTable);
        return new OrderedTree<OID>(new OID("ROOT", null), list);
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
            while(lock.tryLock() == false);
            probabilisticInsertion();
            probabilisticRemoval();
            probabilisticModification();
            lock.unlock();
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
        int p;
        ListIterator<OrderedTree<OID>> iter = tcpConnTable.listIterator();
        if(!iter.hasNext()) return;
        // Probabilistic Removal
        while(iter.hasNext())
        {
            p = random.nextInt(100);
            OID o = iter.next().getRootData();
            if(p < 5)
            {
                switch(o.getValue())
                {
                    case "FIN_WAIT":
                        iter.remove();
                        break;
                    case "LISTEN":
                        iter.remove();
                        break;
                    case "CLOSED":
                        iter.remove();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    void probabilisticModification()
    {
        int p, r;
        OID oid;
        ListIterator<OrderedTree<OID>> iter = tcpConnTable.listIterator();
        if(!iter.hasNext()) return;
        
        //Probabilistic modification
        while(iter.hasNext())
        {
            oid = iter.next().getRootData();
            p = random.nextInt(100);
            if(p < 10)
            {
                switch (oid.getValue())
                {
                    // SYN_SEND -> SYN_RECEIVED
                    case "SYN_SEND":
                        oid.setValue("SYN_RECEIVED");
                        break;
                    // SYN_RECEIVED -> ESTABLISHED
                    case "SYN_RECEIVED":
                        oid.setValue("ESTABLISHED");
                        break;
                    // LISTEN -> SYN_RECEIVED or ESTABLISHED
                    case "LISTEN":
                        r = random.nextInt(2) + 1;
                        oid.setValue(TCP_OPEN_CONN_STATES[r]);
                        break;
                    // ESTABLISHED -> FIN_WAIT
                    case "ESTABLISHED":
                        oid.setValue("FIN_WAIT");
                        break;
                    // FIN_WAIT -> CLOSED
                    case "FIN_WAIT":
                        oid.setValue("CLOSED");
                        break;
                }
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
