package Crypto;



public class Key
{
	private byte[] bytes;
	public Key(byte[] bytes)
	{
		this.bytes = bytes;
	}

	public byte[] getBytes()
	{
		return bytes;
	}
	@Override
	public boolean equals(Object key)
	{
		int i, j, n, m;
		if(key.getClass() != this.getClass())
		{
			return false;
		}
		else
		{
			byte[] bytes = ((Key) key).getBytes();
			n = this.bytes.length;
			m = bytes.length;
			if(n == m)
			{
				j = 0;
				for(i = 0; i < n; i++)
					if(this.bytes[i] == bytes[i]) j++;
			}
			else return false;
		}
		return j == n;
	}
	
	@Override
	public String toString()
	{
		String s = "";
		int n = bytes.length - 1;
		for(int i = 0; i < n; i++)
		{
			s += String.format("%d ", bytes[i]);
		}
		s += String.format("%d", bytes[n]);
		return s;
	}
}
