package Structure;

public class Byte
{
    public static byte[] concat(byte[] a, byte[] b)
    {
        byte[] rv = new byte[a.length + b.length];
        for(int i = 0; i < a.length; i++) rv[i] = a[i];
        for(int i = 0; i < b.length; i++) rv[i + a.length] = b[i];
        return rv;
    }
    
    public static byte[] copy(byte[] a, int offset, int len)
    {
        byte[] rv = new byte[len];
        for(int i = 0; i < len; i++) rv[i] = a[offset + i];
        return rv;
    }
    
    public static byte[] copy(byte[] a, int offset)
    {
        byte[] rv = new byte[a.length - offset];
        for(int i = 0; i < a.length - offset; i++) rv[i] = a[offset + i];
        return rv;
    }
}
