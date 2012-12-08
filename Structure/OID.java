package Structure;

public class OID
{
    private String name;
    private String value;
    
    public OID(String name, String value)
    {
        this.name = name;
        this.value = value;
    }
    public OID(String name)
    {
        this.name = name;
        this.value = null;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getValue()
    {
        return this.value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    public boolean isLeaf()
    {
        return (this.value != null);
    }
    
    @Override
    public String toString()
    {
        return this.name + "=" + this.value;
    }
}
