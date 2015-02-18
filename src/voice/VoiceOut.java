package voice;

import javax.sound.sampled.*;

import voice.Format;

public class VoiceOut{

	    public  TargetDataLine line;
	    private  AudioFormat format;
		
		public VoiceOut()
		{
			format = Format.getAudioFormat();
			try {
				line = AudioSystem.getTargetDataLine(format);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}	
		
	    public  AudioFormat getAudioFormat()
	    {
	    	return format;
	    }
		
}
