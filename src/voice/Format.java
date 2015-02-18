package voice;

/** Am creat o clasa Format care intoarce formatul audio folosit
 *  in timpul chatului;
 */

import javax.sound.sampled.*;

public class Format {
	
	static private int sampleRate = 8000;
	static private int bitsPerSample = 16;
	static private int channels = 1;
	static private boolean signed = true;
	static private boolean bigEndian = true;
	
	static public AudioFormat getAudioFormat()
	{
		return new AudioFormat(sampleRate,bitsPerSample,channels,signed,bigEndian);
	}
	
	static public void printFormat()
	{
		System.out.println("Sample Rate : " + sampleRate);
		System.out.println("Bits per sample : " + bitsPerSample);
		System.out.println("Channels : " + channels);
		System.out.println("Signed : " + signed);
		System.out.println("BigEndian : " + bigEndian);
	}
	
}
