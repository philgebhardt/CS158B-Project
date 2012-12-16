package NESimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import Crypto.*;
import Structure.Byte;
import Structure.OID;
import Structure.User;

/**
 * The TrapHandler class is responsible for listening 
 * and handling trap configuration requests. It is also 
 * responsible for detecting trap events and sending 
 * appropriate notifications.
 * @author Philip Gebhardt
 * @version Fall 2012, CS158B
 *
 */
public class TrapHandler extends Thread
{
    public static final int NOT_EQUAL = 0;
    public static final int LESS_THAN = 1;
    public static final int MORE_THAN = 2;
    public static final int IS_EQUAL = 3;
    
    Lock lock;
    LinkedList<TrapContainer> traps;
    
    public TrapHandler()
    {
        traps = new LinkedList<TrapContainer>();
    }
    
    public void giveLock(Lock l)
    {
        lock = l;
    }
    
    /**
     * The objective of this run method is to continually 
     * check for occurrences of configured trap events. 
     * When this method detects a trap event occurrence, 
     * this method calls upon the notification method.
     */
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            while(lock.tryLock() == false);
            ListIterator<TrapContainer> iter = traps.listIterator();
            while(iter.hasNext())
            {
                int status;
                TrapContainer trap = iter.next();
                if( (status = trap.conditionOccured()) > 0 )
                {
                    try
                    {
                        sendTrapNotification(trap, status);
                    } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                            | InvalidAlgorithmParameterException
                            | ShortBufferException | IllegalBlockSizeException
                            | BadPaddingException | IOException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
            lock.unlock();
        }
    }
    
    /**
     * This method takes a trap event and creates a notification 
     * message, indicating that the event associated with this 
     * trap has occurred.
     * 
     * @param trap - trap to be notified on
     * @param status - status type of the trap's event occurrence
     * @throws UnknownHostException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static void sendTrapNotification(TrapContainer trap, int status) throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        System.out.println(trap.getHost());
        long systemTime = System.currentTimeMillis();
        Socket clientSocket = new Socket(trap.getHost(), 4445);
        InputStream in = clientSocket.getInputStream();
        OutputStream out = clientSocket.getOutputStream();
        Key key = trap.getUser().getKey();
        String message, name, value, type, oidString;
        name = trap.getOID().getName();
        value = trap.getValue();
        oidString = trap.getOID().toString();
        switch(status)
        {
            case NOT_EQUAL:
                type = "!=";
                break;
            case LESS_THAN:
                type = "<";
                break;
            case MORE_THAN:
                type = ">";
                break;
            case IS_EQUAL:
                type = "=";
                break;
            default:
                type = "?";
                break;
        }
        message = "" + systemTime + ":" + name + ":" + type + ":" + value + ":" + oidString;
        byte[] output;
        byte[] iv = Crypto.generateIV(0, 16);
        
        output = iv;
        output = Byte.concat(output, Crypto.AESCBCencrypt(message.getBytes(), key, iv));
        String s = "";
        for(int i = 0; i < output.length; i++) s += String.format("%d ", output[i]);
        System.out.println(s);
        out.write(output);
        System.out.format("Sent: %s%n", new String(output));
        byte[] input = new byte[1000];
        in.read(input);
        in.close();
        out.close();
        clientSocket.close();
    }
    
    public void addTrap(OID oid, int type, String value, String host, User user)
    {
        while(lock.tryLock() == false);
        traps.add(new TrapContainer(oid, type, value, host, user));
        lock.unlock();
    }
    
    public ListIterator<TrapContainer> listIterator()
    {
        return traps.listIterator();
    }
    
    /**
     * TrapContainer is a simple container for all 
     * associated trap information such as the 
     * trap's event value and routines for 
     * detecting whether or not the given trap has 
     * occurred.
     * @author Philip Gebhardt
     * @version Fall 2012, CS158B
     * 
     */
    private class TrapContainer
    {
        private OID oid;
        private int type;
        private String value;
        private String host;
        private User user;
        
        public TrapContainer(OID oid, int type, String value, String host, User user)
        {
            this.oid = oid;
            this.type = type;
            this.value = value;
            this.host = host;
            this.user = user;
        }
        
        public User getUser()
        {
            return this.user;
        }
        
        public OID getOID()
        {
            return oid;
        }
        
        public String getValue()
        {
            return value;
        }
        
        public String getHost()
        {
            return host;
        }
        
        /**
         * Configures a given trap to occurred status.
         * @return the status of the trap
         */
        public int conditionOccured()
        {
            int status = -1;
            switch(this.type)
            {
                case NOT_EQUAL:
                    if( !this.value.equals(this.oid.getValue()) )
                    {
                        status = this.type;
                        this.type = IS_EQUAL;
                    }
                    break;
                case LESS_THAN:
                    if(Integer.parseInt(this.oid.getValue()) < Integer.parseInt(this.value))
                    {
                        status = this.type;
                        this.type = MORE_THAN;
                    }
                    break;
                case MORE_THAN:
                    if(Integer.parseInt(this.oid.getValue()) > Integer.parseInt(this.value)) 
                    {
                        status = this.type;
                        this.type = LESS_THAN;
                    }
                    break;
                case IS_EQUAL:
                    if( this.value.equals(this.oid.getValue()) )
                    {
                        status = this.type;
                        this.type = NOT_EQUAL;
                    }
                    break;
                default:
                    break;
            }
            return status;
        }
    }
    
    public static int trapType(String type)
    {
        int rv = -1;
        switch(type)
        {
            case "!=":
                rv = NOT_EQUAL;
                break;
            case "<":
                rv = LESS_THAN;
                break;
            case ">":
                rv = MORE_THAN;
                break;
            case "=":
                rv = IS_EQUAL;
                break;
            default:
                break;
        }
        return rv;
    }
}
