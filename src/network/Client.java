package network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

import crypto.Aes;
import crypto.Rsa;
import defines.Defines;
import voice.*;

public class Client{
	
	static Socket clientSocket;
	static DataOutputStream outToServer;
	static VoiceOut microphone;
	Rsa keyPair;
	String ip;
	
	/* seteaza ip-ul pe care se va face conexiunea */
	public void setIp(String ip)
	{
		this.ip=ip;

	}
	
	/* seteaza cheia publica */
	public void setPubKey(BigInteger mod)
	{
		keyPair.modulus_en=mod;
	}
	
	/* functie care efectueaza schimbul de chei publice */
	private void publicKeyChange()
	{
		try {
			System.out.println("Bit length cheie : " + keyPair.modulus_de.bitLength());
			/* se trimite cheia public (modulus) deoarece am stabilit ca exp sa ramana la fel pentru ambii 65537 */
			outToServer.write(keyPair.modulus_de.toByteArray(),0,Defines.RSA_KEY_SIZE/8);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* functie care efectueaza schimbul de cheie de sesiune */
	private void sessionKeyChange()
	{
		try {
			BigInteger enc = keyPair.encrypt(new BigInteger(Server.sessionKey));
			outToServer.write(enc.toByteArray(),0,Defines.RSA_KEY_SIZE/8);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* constructor al clientului pe baza perechilor de chei RSA */
	public Client(Rsa keyPair)
	{
		this.keyPair=new Rsa(keyPair);
		microphone = new VoiceOut();
		clientSocket = new Socket();
		ip= new String();
	}
	
	/* functie care proneste threadul convorbire(talk) */
	public  void start()
	{

		try {
			clientSocket.connect(new InetSocketAddress(ip, Defines.PORT), 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		talk.start();
	}
	
	/* seteaza status-ul programului Available si termina orice convorbire */
	public  void stop()
	{
		Defines.STATUS = Defines.AVAILABLE;
	}
	
	
	/* threadul pe care se face convorbirea */
	 Thread talk = new Thread(new Runnable(){
		public void run()
		{
			byte[] data= new byte[Defines.CHUNK];
			int bytesRead;
			try{
				bytesRead=-1;
				microphone.line.open();
				microphone.line.start();
				while(Defines.STATUS == Defines.BUSY && bytesRead!=-1)
				{
					bytesRead = microphone.line.read(data,0,Defines.CHUNK);
					outToServer.write(data,0,Defines.CHUNK);
				}
				clientSocket.close();
				microphone.line.stop();
				microphone.line.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});

	/* metode de test */
	
	
	
	public void startTest() 
	{
		boolean succes = false;
		try {
			clientSocket.connect(new InetSocketAddress(ip, Defines.PORT), Defines.TIMEOUT);
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			succes=true;
		} catch (IOException e) {
			System.out.println("Client invalid Host este offline !");
			succes=false;
		} finally{
			if(succes)
				test.start();
		}
	}	
	
	Thread test = new Thread(new Runnable(){
		public void run()
		{
			byte [] data = new byte[Defines.CHUNK];
			int bytesRead;
			try{
				publicKeyChange();
				Thread.sleep(1500);
			    sessionKeyChange();
				Thread.sleep(1500);
				bytesRead=0;
				System.out.println(Defines.STATUS);
				while(Defines.STATUS == Defines.BUSY && bytesRead!=-1)
				{
					data=Aes.randKey();
					outToServer.write(Aes.encrypt(data, Server.sessionKey),0,Defines.CHUNK);
				}
				clientSocket.close();
			}catch(Exception e){
			}
		}
		
	});
}
