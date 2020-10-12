package MiiTunes;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * 
 */

public class MP3Player implements Runnable
{
    private SourceDataLine line;
    private AudioInputStream din;
    private String path;
    private boolean temp;
    private byte[] data;
    private int nBytesRead, nBytesWritten;
    private SourceDataLine res;
    private AudioInputStream in;
    private boolean paused = false;
    private Object lock;
    
    
    public MP3Player()
    {
        lock = new Object();
    }
    public MP3Player(String filePath)
    {
        path = filePath;
    }
    public void testPlay(String filename)
    {
        try 
        {
            File file = new File(filename);
            in= AudioSystem.getAudioInputStream(file);
            din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                                                                                          baseFormat.getSampleRate(),
                                                                                          16,
                                                                                          baseFormat.getChannels(),
                                                                                          baseFormat.getChannels() * 2,
                                                                                          baseFormat.getSampleRate(),
                                                                                          false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            // Play now. 
            rawplay(decodedFormat, din);
            in.close();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } 

    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException,                                                                                                LineUnavailableException
    {
      data = new byte[4096];
      line = getLine(targetFormat); 
      if (line != null)
      {
        // Start
        line.start();
        nBytesRead = 0;
        nBytesWritten = 0;
        while (nBytesRead != -1)
        {
            nBytesRead = din.read(data, 0, data.length);
            if (nBytesRead != -1) 
                nBytesWritten = line.write(data, 0, nBytesRead);
            synchronized (lock) 
            {
                while ((nBytesRead = din.read(data, 0, data.length)) != -1) 
                {
                    while (paused) 
                    {
                        if(line.isRunning()) 
                        {
                            line.stop();
                        }
                        try 
                        {
                            lock.wait();
                        }
                        catch(InterruptedException e) 
                        {
                        }                        
                    }	
		if(!line.isRunning()) 
                {
			line.start();
		}
                line.write(data, 0, nBytesRead);
                }
            }
        }
        // Stop
        line.drain();
        line.stop();
        line.close();
        din.close();
      } 
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
    {
      res = null;
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      res = (SourceDataLine) AudioSystem.getLine(info);
      res.open(audioFormat);
      return res;
    } 
    
    public boolean isActive()
    {
        try
        {
            if(line.isActive() == true)
                temp = true;
            else 
                temp = false;
        }
        catch (NullPointerException e)
        {
        }
        finally
        {
            return temp;
        }
    }
    public boolean isOpen()
    {
        try
        {
            if(line.isOpen() == true)
                temp = true;
            else 
                temp = false;
        }
        catch (NullPointerException e)
        {
        }
        finally
        {
            return temp;
        }
    }
    public boolean isRunning()
    {
        try
        {
            if(line.isRunning() == true)
                temp = true;
            else 
                temp = false;
        }
        catch (NullPointerException e)
        {
        }
        finally
        {
            return temp;
        }
    }
    public void stopPlay() throws IOException
    {
        
        if(line.isActive())
        {
            paused = true;
            line.stop();
            line.flush();
            line.close();
            din.close();
            in.close();
        }
    }
    
    public void userPressedPause() 
    {
        paused = true;
    }
    
    public void userPressedPlay() 
    {
        synchronized(lock) 
        {
            paused = false;
            lock.notifyAll();
        }    
    }
    public void setPath(String filePath)
    {
        path = filePath;
    }
    public String getPath()
    {
        return path;
    }
    @Override
    public void run() 
    {
        if(paused == true)
        {
            synchronized(lock) 
            {
                paused = false;
                lock.notifyAll();
            }
        }
        else
            testPlay(path);
    }
}