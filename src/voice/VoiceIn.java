package voice;

import javax.sound.sampled.*;

import voice.Format;

public class VoiceIn{

		public  SourceDataLine line;
	    private  AudioFormat format;
		
	    public VoiceIn()
		{
			format = Format.getAudioFormat();
			try {
				line = AudioSystem.getSourceDataLine(format);
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
