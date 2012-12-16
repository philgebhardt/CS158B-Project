package NESimulator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import Structure.*;

/**
 * The Element class is responsible for simulating a live
 * network element, in this case a network router. The
 * simulation includes randomly inserting, removing, and
 * modifying elements of this simulated router's TCP
 * connection table.
 * 
 * @author Philip Gebhardt
 * @version Fall 2012, CS158B
 * 
 */
public class Element extends Thread
{
    static final long REFRESH_RATE = 200; //200 milliseconds
    static String[] TCP_OPEN_CONN_STATES =
    { "SYN_SEND", "SYN_RECEIVED", "ESTABLISHED", "LISTEN" };
    static String[] TCP_CLOSE_CONN_STATES =
    { "FIN_WAIT_1", "TIMED_WAIT", "CLOSE_WAIT", "FIN_WAIT_2", "LAST_ACK",
            "CLOSED" };

    OID tcpTableSize;
    OrderedTree<OID> tcpConnTable;
    OrderedTree<OID> system;
    Lock lock;
    
    static final Random random = new Random();

    public Element(LinkedList<OrderedTree<OID>> systemInfo)
    {
        this.tcpTableSize = new OID("CurrConnections", "0");
        this.tcpConnTable = new OrderedTree<OID>(new OID("TCP_CONN_TABLE", null), new LinkedList<OrderedTree<OID>>());
        this.system = new OrderedTree<OID>(new OID("SYSTEM", null), systemInfo);
    }
    
    public void giveLock(Lock lock)
    {
        this.lock = lock;
    }
    
    public Lock getLockObject()
    {
        return lock;
    }

    /**
     * This method returns all of this Element instance's
     * simulation data within a OrderedTree<OID>
     * 
     * @return instance data encapsulated in an OrderedTree<OID>
     */
    public OrderedTree<OID> agentData()
    {
        LinkedList<OrderedTree<OID>> tcpList = new LinkedList<OrderedTree<OID>>();
        LinkedList<OrderedTree<OID>> list = new LinkedList<OrderedTree<OID>>();
        LinkedList<OrderedTree<OID>> rootList = new LinkedList<OrderedTree<OID>>();
        
        tcpList.add(tcpConnTable);
        tcpList.add(new OrderedTree<OID>(tcpTableSize, null));
        OrderedTree<OID> tcp = new OrderedTree<OID>(new OID("TCP"), tcpList);
        
        list.add(system);
        list.add(tcp);
        rootList.add(new OrderedTree<OID>(new OID("MIB-2", null), list));
        
        return new OrderedTree<OID>(new OID("ROOT"), rootList);
    }

    /**
     * The objective of this method is to continually introduce 
     * random events into the NESimulator. This method achieves 
     * this by random insertions, deletions, and modifications 
     * to the Element instance's data field.
     */
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

    /**
     * Probabilistically adds a randomly generated TCP 
     * connection to this instance's data field 
     * approximately 1 out of every 4 times this method 
     * is properly called.
     */
    void probabilisticInsertion()
    {
        int p;
        // Probabilistic Insertion
        p = random.nextInt(100);
        if (p < 25)
        {
            OID newOID = createRandomTCP_OID();
            tcpConnTable.listIterator().add(new OrderedTree<OID>(newOID, null));
            incrementTcpTableSize();
        }
    }

    /**
     * Probabilistically removes randomly selected TCP 
     * connections from this instance's data field.
     * When this method is called, every TCP connection
     * with a status of FIN_WAIT, LISTEN, or CLOSED has
     * a 5% chance of being removed from the data field.
     */
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
                        decrementTcpTableSize();
                        break;
                    case "LISTEN":
                        iter.remove();
                        decrementTcpTableSize();
                        break;
                    case "CLOSED":
                        iter.remove();
                        decrementTcpTableSize();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Probabilistically modifies elements within this Element
     * instance's data field. Every modification is essentially 
     * a logical progression between TCP connection states. 
     * When this method is called, each element within the 
     * instance's data has a 10% chance of being modified.
     */
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

    /**
     * Creates a randomly generated TCP connection 
     * with a source and destination IP address pair, 
     * a source and destination socket pair, and a 
     * connection status.
     * @return the OID that was generated.
     */
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
    
    void incrementTcpTableSize()
    {
        int i = Integer.parseInt(tcpTableSize.getValue());
        i++;
        tcpTableSize.setValue(""+i);
    }
    void decrementTcpTableSize()
    {
        int i = Integer.parseInt(tcpTableSize.getValue());
        i--;
        tcpTableSize.setValue(""+i);
    }
}
