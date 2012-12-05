import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class MyLock implements Lock
{
    private boolean acquired;
    
    public MyLock()
    {
        acquired = false;
    }

    @Override
    public void lock()
    {
        if(acquired)return;
        else acquired = true;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Condition newCondition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean tryLock()
    {
        if(acquired) return false;
        else
        {
            acquired = true;
            return true;
        }
    }

    @Override
    public boolean tryLock(long arg0, TimeUnit arg1)
            throws InterruptedException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void unlock()
    {
        if(acquired) acquired = false;
        else return;
    }

}
