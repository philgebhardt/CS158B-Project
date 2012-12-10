package RMON;

public class User
{
    private String name;
    private Key key;
    private Key rmonKey;
    
    public User(String name, Key key, Key rmonKey)
    {
        this.name = name;
        this.key = key;
        this.rmonKey = rmonKey;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setKey(Key key)
    {
        this.key = key;
    }
    public void setRMONKey(Key key)
    {
        this.key = key;
    }
    public String getName()
    {
        return name;
    }
    public Key getKey()
    {
        return key;
    }
    public Key getRMONKey()
    {
    	return rmonKey;
    }
    @Override
    public String toString()
    {
        return "" + name + " " + key.toString();
    }
}
