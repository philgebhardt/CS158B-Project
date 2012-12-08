package Structure;
import Crypto.*;

public class User
{
    private String name;
    private Key key;
    
    public User(String name, Key key)
    {
        this.name = name;
        this.key = key;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setKey(Key key)
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
    @Override
    public String toString()
    {
        return "" + name + " " + key.toString();
    }
}
