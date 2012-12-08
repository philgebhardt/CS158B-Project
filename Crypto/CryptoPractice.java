package Crypto;

public class CryptoPractice
{
	public static void main(String args[])
	{
		String message = "Hello World!";
		byte[] iv = Crypto.generateIV(0, 16);
		Key key = new Key(new byte[]{
				0x00, 0x01, 0x02, 0x03, 
				0x04, 0x05, 0x06, 0x07, 
				0x08, 0x09, 0x0a, 0x0b, 
				0x0c, 0x0d, 0x0e, 0x0f
				});
		try
		{
			String ciphertext = Crypto.AESCBCencrypt(message, key, iv);
			System.out.format("Ciphertext: %s (IV=%s)%n", ciphertext, new String(iv));
			
			String plaintext = Crypto.AESCBCdecrypt(ciphertext, key, iv);
			System.out.format("Plaintext: %s%n", plaintext);
		}
		catch(Exception e)
		{
			System.err.format("Bad encryption: %s%n", e.getMessage());
		}
	}
}
