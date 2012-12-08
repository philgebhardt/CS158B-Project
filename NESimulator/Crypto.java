
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
    /**
     * Encrypts a string using the AES algorithm in CBC mode with PKCS5Padding. 
     * This encryption uses the key bytes specified by key, and the initialization
     * vector bytes specified by iv.
     * 
     * @param message - plaintext
     * @param key - key
     * @param iv - initialization vector
     * @return the encrypted message
     * 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String AESCBCencrypt(String message, Key key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        int ciphertext_len;
        byte[] input, keyBytes, ciphertext;
        SecretKeySpec keySpec;
        IvParameterSpec ivSpec;
        Cipher cipher;
        
        input = message.getBytes();
        keyBytes = key.getBytes();
        
        keySpec = new SecretKeySpec(keyBytes, "AES");
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        ciphertext = new byte[cipher.getOutputSize(input.length)];
        
        ciphertext_len = cipher.update(input, 0, input.length, ciphertext, 0);
        ciphertext_len += cipher.doFinal(ciphertext, ciphertext_len);
        return new String(ciphertext);
    }
    
    /**
     * Decrypts a string using the AES algorithm in CBC mode with PKCS5Padding. 
     * This decryption uses the key bytes specified by key, and the initialization
     * vector bytes specified by iv.
     * 
     * @param ciphertext - encrypted message
     * @param key - key
     * @param iv - initialization vector
     * @return the decrypted message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String AESCBCdecrypt(String ciphertext, Key key, byte[] iv)
    		throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
    		ShortBufferException, IllegalBlockSizeException, BadPaddingException
    {
        int plaintext_len;
        byte[] input, keyBytes, plaintext;
        SecretKeySpec keySpec;
        IvParameterSpec ivSpec;
        Cipher cipher;
        
        input = ciphertext.getBytes();
        keyBytes = key.getBytes();
        
        keySpec = new SecretKeySpec(keyBytes, "AES");
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        plaintext = new byte[cipher.getOutputSize(input.length)];
        
        plaintext_len = cipher.update(input, 0, input.length, plaintext, 0);
        plaintext_len += cipher.doFinal(plaintext, plaintext_len);
        return new String(plaintext);
    }
    
    /**
     * Generates an initialization vector, an array of length
     * byteLength. This array is randomized by the Random class
     * with the seed specified by the input parameter seed.
     * 
     * @param seed - seed for randomization
     * @param byteLength - length of byte array
     * @return array of random bytes
     */
    public static byte[] generateIV(int seed, int byteLength)
    {
    	byte[] iv = new byte[byteLength];
    	Random random = new Random(seed);
    	random.nextBytes(iv);
    	return iv;
    }
}
