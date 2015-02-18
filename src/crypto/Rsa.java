package crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

    

public class Rsa {
   private final static BigInteger one      = new BigInteger("1");
   private final static SecureRandom random = new SecureRandom();

   private BigInteger privateKey;
   public BigInteger publicKey;
   public BigInteger modulus_de; 		// cheia publica generata de program
   public BigInteger modulus_en;		// cheia publica primita de la celalalt peer

   // generate an N-bit (roughly) public and private key
   public Rsa(int N) {
      BigInteger p = BigInteger.probablePrime(N/2, random);
      BigInteger q = p.nextProbablePrime();
      BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

      modulus_de    = p.multiply(q);                                  
      publicKey  = new BigInteger("65537");   // common value in practice = 2^16 + 1
      privateKey = publicKey.modInverse(phi);
   }
   
   public static BigInteger getCoprime(BigInteger m) {
	      int length = m.bitLength()-1;
	      BigInteger e = BigInteger.probablePrime(length,random);
	      while (! (m.gcd(e)).equals(BigInteger.ONE) ) {
	      	 e = BigInteger.probablePrime(length,random);
	      }
	      return e;
	   }


   public Rsa(Rsa keyPair) {
	   this.modulus_de = keyPair.modulus_de;
	   this.publicKey = keyPair.publicKey;
	   this.privateKey = keyPair.privateKey;
   }
   
   public void setEncKey(BigInteger mod)
   {
	   modulus_en=mod;
   }
   
   public BigInteger encrypt(BigInteger message) {

      return message.modPow(publicKey, modulus_en);
   }

   public BigInteger decrypt(BigInteger encrypted) {
      return encrypted.modPow(privateKey, modulus_de);
   }

   public String toString() {
      String s = "";
      s += "public  = " + publicKey  + "\n";
      s += "private = " + privateKey + "\n";
      s += "modulus = " + modulus_de;
      return s;
   }
 
}
