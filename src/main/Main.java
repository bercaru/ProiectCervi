package main;


import network.Server;
import defines.Defines;

public class Main {

	
	public static void main(String[] args) throws Exception {
		
		Server.listenTest();
		Server.callTest("172.16.15.233");

		
		
		Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Defines.STATUS = Defines.AVAILABLE;
        		Server.callTest("172.16.15.233");

            }
        });
        stopper.start();

	}
	
}
	
