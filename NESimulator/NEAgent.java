
public class NEAgent extends Thread
{
	OrderedTree<String> data;
	
	public NEAgent(OrderedTree<String> data)
	{
		this.data = data;
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
    	    System.out.print(data.toString());
	    }
	}
}
