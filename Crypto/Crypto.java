package Crypto;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypto
{
    public static byte[] AESCBCencrypt(byte[] input, Key key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
    {
        int ciphertext_len;
        byte[] keyBytes, ciphertext;
        SecretKeySpec keySpec;
        IvParameterSpec ivSpec;
        Cipher cipher;
        
        keyBytes = key.getBytes();
        keySpec = new SecretKeySpec(keyBytes, "AES");
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        ciphertext = new byte[cipher.getOutputSize(input.length)];
        
        ciphertext_len = cipher.update(input, 0, input.length, ciphertext, 0);
        ciphertext_len += cipher.doFinal(ciphertext, ciphertext_len);
        return ciphertext;
    }
    
    public static byte[] AESCBCdecrypt(byte[] input, Key key, byte[] iv)
    		throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
    		ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
    {
        int plaintext_len;
        byte[] keyBytes, plaintext;
        SecretKeySpec keySpec;
        IvParameterSpec ivSpec;
        Cipher cipher;

        keyBytes = key.getBytes();
        keySpec = new SecretKeySpec(keyBytes, "AES");
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        plaintext = new byte[cipher.getOutputSize(input.length)];
        
        plaintext_len = cipher.update(input, 0, input.length, plaintext, 0);
        plaintext_len += cipher.doFinal(plaintext, plaintext_len);
        return plaintext;
    }
    
    public static byte[] generateIV(int seed, int byteLength)
    {
    	byte[] iv = new byte[byteLength];
    	Random random = new Random(seed);
    	random.nextBytes(iv);
    	return iv;
    }
}
