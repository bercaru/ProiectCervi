/**	LOGICA PROGRAMULUI ESTE SIMPLA:
 *  LA DESCHIDEREA PROGRAMULUI SE APELEAZA FUNCTIA DE LISTEN DIN CLASA SERVER
 *  CLASA SERVER CONTINE UN THREAD CARE ASTEAPTA APELURI PRIMESTE DATE (SPEAKERS)
 *  PENTRU A SUNA FOLOSIM CLASA CLIENT CARE CONTINE UN THREAD PENTRU A TRANSIMTE DATE (MICROPHONE)
 *  PE SCURT INTR-O CONVERSATIE FIECARE PARTICIPANT ARE 2 THREADURI UN SERVER SI UN CLIENT
 *  UN THREAD PENTRU A VORBI SI ALT THREAD PENTRU A ASCULTA
 */


package network;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import crypto.*;
import defines.Defines;
import voice.*;

public class Server{
	
	static ServerSocket welcome;
	static Socket serverSocket;
	static DataInputStream inFromClient;
	static VoiceIn speakers;
	static Rsa keyPair;
	public static byte[] sessionKey;
	static Client callClient;
	
	
	
	/* generare de cheie de sesiune pentru criptarea AES */
	private static void genKey()
	{
		sessionKey = new byte[Defines.AES_KEY_SIZE];
		sessionKey =  Aes.randKey();
	}
	
	/* functie prin care se efectueaza schimbulde chei publice */
	private static void publicKeyChange()
	{
		
		byte[] modulus= new byte[Defines.RSA_KEY_SIZE/8];
		
		try {
			/* Citeste modulul pe 1024 biti */
			inFromClient.read(modulus,0,Defines.RSA_KEY_SIZE/8);
			/*salveaza cheia publica in keyPair*/
			keyPair.setEncKey(new BigInteger(modulus));	
			callClient.setPubKey(new BigInteger(modulus));
			System.out.println(keyPair.modulus_en);
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* functie care face xor cu cheia de sesiune generata initial si cheia de sesiune straina*/
	private static void keyXOR(byte[] foreignKey)
	{
		int i=0;
		for(byte b : foreignKey)
		{
			sessionKey[i]=(byte) (sessionKey[i++]^b);
		}
		
	}
	
	/* functie prin care se face schimbul de chei de sesiune */
	private static void sessionKeyChange() throws InterruptedException
	{
		byte[] buff= new byte[Defines.RSA_KEY_SIZE/8];
		
		try {
			System.out.println("SessionKey : " + Server.bytesToHex(sessionKey));
			inFromClient.read(buff,0,Defines.RSA_KEY_SIZE/8);
			buff = keyPair.decrypt(new BigInteger(buff)).toByteArray();
			System.out.println("ForeignKey : " + Server.bytesToHex(buff));
			Thread.sleep(1500);
			keyXOR(buff);
			System.out.println("FinalKey : " + Server.bytesToHex(sessionKey));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* returneaza ip-ul celui care apeleaza */
	private static String getRemoteIp(String address)
	{
		String remoteIp = serverSocket.getRemoteSocketAddress().toString();
		remoteIp=remoteIp.split(":")[0];
		remoteIp=remoteIp.split("/")[1];
		return remoteIp;
	}
	
	/* accepta conexiuni trebuie ca Statusul sa fie AVAILABLE */
	private static void accept() throws IOException
	{
		System.out.println("Accept Conexiuni !");
		serverSocket = welcome.accept();  // accepta conexiuni de la client
		System.out.println("Cineva s-a conectat" + serverSocket.getRemoteSocketAddress());
		inFromClient = new DataInputStream(serverSocket.getInputStream());
		
		/* daca este disponibil creeaza un client de apelare
		 * care se conecteaza la serverul cu ip-ul pe care primeste voce
		 * daca nu este disponibil inseamna ca serverul a fost sunat de cineva
		 */
		
		if(Defines.STATUS == Defines.AVAILABLE)
		{
			callClient=new Client(keyPair);
			callClient.setIp(getRemoteIp(serverSocket.getRemoteSocketAddress().toString()));
			callClient.start();
			System.out.println("Am pornit clientul de raspuns ! ");
		}
		Defines.STATUS = Defines.BUSY; 
	}
	
	/* functie simpla de apelare */
	public static void call(String ip) throws UnknownHostException, IOException
	{
		callClient=new Client(keyPair);
		callClient.setIp(getRemoteIp(serverSocket.getRemoteSocketAddress().toString()));
		callClient.start();
		Defines.STATUS = Defines.BUSY;
	}
	
	/* functia care porneste thredul de ascultare */
	public static void listen()
	{
		speakers = new VoiceIn();
		
		try {
			welcome = new ServerSocket(Defines.PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Defines.STATUS = Defines.AVAILABLE;
		System.out.println("Am pornit Serverul !");
		listen.start();
		
	}

	/* threadul de ascultare*/
	static Thread listen = new Thread(new Runnable(){
		public void run()
		{
			byte[] data= new byte[Defines.CHUNK];
			int bytesRead;
			
			try{	
				do{
					bytesRead=0;
					/*statusul este available si se asteapta conexiuni*/
					accept();
					speakers.line.open();
					speakers.line.start();
					
					while(Defines.STATUS == Defines.BUSY && bytesRead!=-1)
					{
						bytesRead = inFromClient.read(data,0,Defines.CHUNK);
						speakers.line.write(data,0,Defines.CHUNK);
						//System.out.println("boxele ar trebui sa se adua acum");
					}
					
					/* se inchide reseteaza socketul si se inchide linia pe care se reda sunetul */
					speakers.line.stop();
					speakers.line.close();
					serverSocket.close();
					
					System.out.println("The call has ended !");
					/* se  face status serverul fiind Available */
					Defines.STATUS=Defines.AVAILABLE;
					
					/* se reia loopul pentru a accepta noi conexiuni*/
				}while(Defines.STATUS!=Defines.OFFLINE);
				welcome.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});


	
	/*				*
	 *   TESTARE    *
	 *  			*/
	
	
	/* functie care transforma din bytes in hexa */
	final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static void acceptTest() throws IOException
	{
		System.out.println("Accept Conexiuni !");
		serverSocket = welcome.accept();  // accepts connection from a client
		System.out.println("Cineva s-a conectat" + serverSocket.getRemoteSocketAddress());
		inFromClient = new DataInputStream(serverSocket.getInputStream());
		
		/* daca este disponibil creeaza un client de apelare
		 * care se conecteaza la serverul cu ip-ul pe care primeste voce
		 * daca nu este disponibil inseamna ca serverul a fost sunat de cineva
		 */
		if(Defines.STATUS == Defines.AVAILABLE)
		{
			callClient=new Client(keyPair);
			genKey();
			callClient.setIp(getRemoteIp(serverSocket.getRemoteSocketAddress().toString()));
			callClient.startTest();
			System.out.println("Am pornit clientul de raspuns ! ");
		}
		
		Defines.STATUS = Defines.BUSY; 
	}
	
	public static void listenTest()
	{
		speakers = new VoiceIn();
		keyPair = new Rsa(Defines.RSA_KEY_SIZE-1);
		System.out.println(keyPair.toString());
		
		try {
			welcome = new ServerSocket(Defines.PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Defines.STATUS = Defines.AVAILABLE;
		System.out.println("Am pornit Serverul !");
		test.start();
		
	}
	
	public static void callTest(String ip)
	{		
		Defines.STATUS = Defines.BUSY;
		callClient=new Client(keyPair);
		/*seteaza cheia de sesiune pentru convorbirea curenta*/
		genKey();
		callClient.setIp(ip);
		callClient.startTest();			


	}
	
	static Thread test = new Thread(new Runnable(){
		public void run()
		{
			byte[] data= new byte[Defines.CHUNK];
			int bytesRead;
			
			try{	
				do{
					bytesRead=0;
					acceptTest();
					publicKeyChange();
					sessionKeyChange();
					while(Defines.STATUS == Defines.BUSY && bytesRead!=-1)
					{
						/*aici se citesc bucati de 16 bytes*/
						bytesRead = inFromClient.read(data,0,Defines.CHUNK);
						System.out.println(bytesToHex(Aes.decrypt(data, sessionKey)));
						
					}
					
					serverSocket.close();
					
					System.out.println("The call has ended !");
					// se  face status serverul fiind Available
					Defines.STATUS=Defines.AVAILABLE;
					
				}while(Defines.STATUS!=Defines.OFFLINE);
				welcome.close();
			}catch(Exception e){
			}
		}
	});
}
