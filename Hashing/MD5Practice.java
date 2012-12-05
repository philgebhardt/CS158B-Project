package Hashing;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Practice
{
	public static void main(String args[]) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		//Create a message digest instance that will use MD5 algorithm
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		//Passwords to be hashed
		String pwd1 = "mypassword";
		String pwd2 = "myOTHERpassword";
		
		//Parse password strings into array of bytes according to UTF-16 Character set
		//The character set specification is necessary if someone uses unorthodox characters
		//as a password.
		byte[] input1 = pwd1.getBytes("UTF-16");
		byte[] input2 = pwd2.getBytes("UTF-16");
		
		byte[] hash1 = md.digest(input1);
		byte[] hash2 = md.digest(input2);
		Key key1 = new Key(hash1);
		Key key2 = new Key(hash2);
		
		//Print Statements
		System.out.format("Password1: %s%n", pwd1);
		System.out.format("Password2: %s%n", pwd2);
		
		System.out.println("Bytes of hashed passwords:");
		for(int i = 0; i < hash1.length; i++)
			System.out.format("%d ", hash1[i]);
		System.out.print("\n");
		for(int i = 0; i < hash2.length; i++)
			System.out.format("%d ", hash2[i]);
		System.out.print("\n");
		
		System.out.format("Do are these keys equal?: %b", key1.equals(key2));
	}
}