package crypto;

import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import defines.Defines;



public class Aes {

	
	/* genereaza o cheie aes random de lungime AES_KEY_SIZE */
	public static byte[] randKey()
	{
		Random ranGen = new SecureRandom();
		byte[] aesKey = new byte[Defines.AES_KEY_SIZE]; // 16 bytes = 128 bits
		ranGen.nextBytes(aesKey);
		/* cheie trebuie sa fie un numar pozitiv alfel avem probleme la criptarea rsa */
		int d = aesKey[0];
		if(d<0) d=d+128;
		aesKey[0]=(byte)d;
		return aesKey;
	}
    
	/* pentru operatiile de criptare si decriptare folosim AES/ECB/NoPadding */
	
    public static byte[] encrypt(byte[] voiceBlock, byte[] aesKey) throws Exception {
    	
	    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
	    SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    return cipher.doFinal(voiceBlock);
  }
 
  public static byte[] decrypt(byte[] voiceBlock,byte[] aesKey) throws Exception{

	    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
	    SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    return cipher.doFinal(voiceBlock);   
  }
}

