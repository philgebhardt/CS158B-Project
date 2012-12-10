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
    
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e1)
            {
                // TODO Auto-generated catch block
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
    
    public static void sendTrapNotification(TrapContainer trap, int status) throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        long systemTime = System.currentTimeMillis();
        Socket clientSocket = new Socket(trap.getHost(), 4446);
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
        message = "" + systemTime + " " + "Trap Notification: " + name + " value " + type + " " + value + " " + oidString;
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
        
        public void setUser(User user)
        {
            this.user = user;
        }
        
        public OID getOID()
        {
            return oid;
        }
        
        public void setOID(OID oid)
        {
            this.oid = oid;
        }
        
        public int getType()
        {
            return type;
        }
        
        public void setType(int type)
        {
            this.type = type;
        }
        
        public String getValue()
        {
            return value;
        }
        
        public void setValue(String value)
        {
            this.value = value;
        }
        
        public String getHost()
        {
            return host;
        }
        
        public void setHost(String host)
        {
            this.host = host;
        }
        
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
