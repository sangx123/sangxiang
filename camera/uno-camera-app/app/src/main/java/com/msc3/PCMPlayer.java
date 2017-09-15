package com.msc3;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import com.hubble.registration.PublicDefine;
import com.media.ffmpeg.FFMpegPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PCMPlayer implements Runnable {

  public static final int BUFFER_EMPTY = 0;
  public static final int BUFFER_PROCESSING = 1;
  public static final int BUFFER_FULL = 2;

  private boolean _isAlive;
  private ArrayList<byte[]> _buffers;
  private AudioTrack _at;

  private static final int NUM_BUFF = 16;
  private static final int ONE_BUFF_SIZE = 8 * 1000;

  private byte s_buffers[][];
  private int s_buffers_len[]; //Store len of the s_buffers, it can be < 8000
  private int s_buffers_offset[]; //store the offset to continue to read
  private double s_buffers_pts[];

  private int s_buffer_status[]; //status of buffer EMPTY , INPROCESS,  FULL
  private int write_next, read_next;
  private int minBufferSize;
  private int maxBufferSize;
  private int overallDataBuffered = 0;

  private FFMpegPlayer ffmpeg_callback = null;

  //DBG
  private String filename;
  private FileOutputStream os;

  private int pcmFreq;
  private int numChannels;

  private boolean forPlaybackOrRelay = false;

  private boolean debug_write_to_file = false;
  private boolean shouldMuteAudio = false;


  public PCMPlayer(int freq, int num_channels, boolean forPlayback) {
    _isAlive = true;
    _buffers = new ArrayList<byte[]>();

    /// new way - hardcode buffer
    s_buffers = new byte[NUM_BUFF][ONE_BUFF_SIZE];
    s_buffers_len = new int[NUM_BUFF];
    s_buffers_offset = new int[NUM_BUFF];
    s_buffer_status = new int[NUM_BUFF];
    s_buffers_pts = new double[NUM_BUFF];
    read_next = 0;
    write_next = 0;

    if (debug_write_to_file) {
      String path = Environment.getExternalStorageDirectory().getPath() + File.separator;
      filename = path + "test.pcm";
      Log.d("mbp", "AUDIO data is " + filename);
    }


    for (int i = 0; i < NUM_BUFF; i++) {
      s_buffer_status[i] = BUFFER_EMPTY;
      s_buffers_offset[i] = 0;
      s_buffers_len[i] = 0;
      s_buffers_pts[i] = 0;
    }

    pcmFreq = freq;
    numChannels = num_channels;
    forPlaybackOrRelay = forPlayback;
  }


  public void setFFMpegUpdateClkCb(FFMpegPlayer player) {
    ffmpeg_callback = player;
  }

  /**
   * Set audio muted/unmuted. When muted, we simply don't write data to AudioTrack
   * buffer.
   *
   * @param shouldMute
   */
  public void setAudioMuted(boolean shouldMute) {
    shouldMuteAudio = shouldMute;
  }

  public void flush() {
    read_next = 0;
    write_next = 0;

    for (int i = 0; i < NUM_BUFF; i++) {
      s_buffer_status[i] = BUFFER_EMPTY;
      s_buffers_offset[i] = 0;
      s_buffers_len[i] = 0;
      s_buffers_pts[i] = 0;
    }

    if (_at != null) {
      _at.flush();
    }
    overallDataBuffered = 0;
  }

  public void pause() {
    if (_at != null) {
      try {
        _at.pause();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  public void resume() {
    if (_at != null && _at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
      try {
        _at.play();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  public void run() {

    int overallData = 0;

    boolean startedPlaying = false;

//		minBufferSize = AudioTrack.getMinBufferSize(8000, 
//				AudioFormat.CHANNEL_CONFIGURATION_MONO,
//				AudioFormat.ENCODING_PCM_16BIT);

    minBufferSize = AudioTrack.getMinBufferSize(pcmFreq,
        numChannels,
        AudioFormat.ENCODING_PCM_16BIT);
    //ihomephone: minBufferSize ~ 2k

    //OK CODE: maxBufferSize = minBufferSize *6;
    /*also ok maxBufferSize = minBufferSize *4;

		if (maxBufferSize < ONE_BUFF_SIZE)
		{
			maxBufferSize =ONE_BUFF_SIZE;
		}*/

    maxBufferSize = minBufferSize * 2; // * 4;
    //maxBufferSize = 8000;

    if (minBufferSize >= 2972) //iHOME phone
    {
      maxBufferSize = minBufferSize;
    }

    _at = new AudioTrack(AudioManager.STREAM_MUSIC,
//				8000, 
//				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
        pcmFreq,
        numChannels,
        AudioFormat.ENCODING_PCM_16BIT,
        maxBufferSize,
        AudioTrack.MODE_STREAM);

    if (debug_write_to_file) {
      initWriteTofile();
    }

    Log.d("mbp", "minBufferSize: " + minBufferSize +
        " maxBuff:" + maxBufferSize + ", isAlive? " + _isAlive);

    overallDataBuffered = 0;
    int written;
    int toBeWritten;
    while (_isAlive) {

      while (_at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED && _isAlive) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      written = 0;
      if (s_buffer_status[read_next] != BUFFER_FULL) {
//				Log.d("mbp","Buffer empty ..read_next: " + read_next +
//						" write_next:" + write_next); 
        try {
          Thread.sleep(20);
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      } else {

        s_buffer_status[read_next] = BUFFER_PROCESSING;
        //synchronized(s_buffers)
        {

					/*  */
//					if (startedPlaying)
//					{
//
//						if (_at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED)
//						{
//							Log.d("mbp", "flushing audio data");
//							_at.flush();//flush old data to avoid delay
//							_at.play();
//						}
//					}


          if (s_buffers_len[read_next] > 0) {
            /* IMPORTANT NOTE:
						 *  write () will not really write any thing IF the 
						 *  audiotrack internal buffer(set when created above) 
						 *  is full and it's not been played (play()) not called
						 *
						 *  @written: is the returned number of bytes written - 
						 *   it could be SMALLER than the the actual s_buffers_len[read_next]
						 *   NEED to manage this to make sure data is written completely.
						 */
            toBeWritten = s_buffers_len[read_next] - s_buffers_offset[read_next];
//						Log.d("mbp","befor write: " + toBeWritten + " offset: " + s_buffers_offset[read_next] );
						
						/* If audio is muted, we don't need to write data to AudioTrack buffer,
						 * so that users do not hear audio, just update audio clock for sync purpose. */
            if (shouldMuteAudio == false) {
              written = _at.write(s_buffers[read_next], s_buffers_offset[read_next], toBeWritten);
            }
//						Log.d("mbp", "after write: " + written); 

//						if ( (written >= toBeWritten)||
//								(_at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) ||
//								(_at.getPlayState() == AudioTrack.PLAYSTATE_STOPPED)
//							)
            //Log.d("mbp", "shouldMuteAudio: " + shouldMuteAudio);
            if (shouldMuteAudio == true) {
							/* Audio muted, just update audio clk and buffer status. */
              updateAudioClk(s_buffers_pts[read_next]);
              s_buffers_pts[read_next] = 0;
              s_buffers_len[read_next] = 0;
              s_buffers_offset[read_next] = 0;
              s_buffer_status[read_next] = BUFFER_EMPTY;
              read_next = (read_next + 1) % NUM_BUFF;

              long sleep_time = (int) (toBeWritten * 1000 / 16000.0);
//							Log.d("mbp", "shouldMuteAudio, toBeWritten " + toBeWritten +
//									", sleep_time: " + sleep_time);
              try {
                Thread.sleep(sleep_time);
              } catch (InterruptedException e) {
              }
              overallDataBuffered -= toBeWritten;
            } else {
              if (written >= toBeWritten) {
                //finish writing , or incase of pause, discard this audio buff
                updateAudioClk(s_buffers_pts[read_next]);
                s_buffers_pts[read_next] = 0;
                s_buffers_len[read_next] = 0;
                s_buffers_offset[read_next] = 0;
                s_buffer_status[read_next] = BUFFER_EMPTY;
                read_next = (read_next + 1) % NUM_BUFF;
                //Log.d("mbp", "read_next: " + read_next);
                overallDataBuffered -= written;
              } else if (written > 0) {
                Log.d("mbp", "written: " + written
                        + " tobewritten" + toBeWritten
                        + " dataLen: " + s_buffers_len[read_next]
                        + " playState: " + _at.getPlayState()
                        + " head: " + _at.getPlaybackHeadPosition()
                );
                //remember the position where we read until
                s_buffers_offset[read_next] += written;
                //force this buffer to FULL again
                s_buffer_status[read_next] = BUFFER_FULL;
                //dont increment read_next
                overallDataBuffered -= written;
              } else if (written <= 0) {
                s_buffer_status[read_next] = BUFFER_FULL;
              }
            }


            overallData += written;

						/* We start to play only AFTER we write more than 
						 *1sec of data (16000) //minBufferSize of data 
						 */
            if ((overallData >= maxBufferSize) &&
                //(overallDataBuffered > 16000 ) &&
                (startedPlaying == false)) {
              Log.d("mbp", "start play at: " + overallData);
              _at.play();
              startedPlaying = true;
              start_time = System.currentTimeMillis();
            }

          } else {

            s_buffers_offset[read_next] = 0;
            s_buffer_status[read_next] = BUFFER_EMPTY;
            read_next = (read_next + 1) % NUM_BUFF;
            //Log.d("mbp", "read_next: " + read_next + " s_buffers_len == 0)");
          }

        }//synch block
      }

    }

    _at.flush();
    try {
      _at.stop();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
    _at.release();
  }

	/* 20120407: check for db level over 100ms block */
  /**
   * simple logic: if in any block.. we find that the db level raises higher than THRESHOLD
   * we don't mute -- actual sound is coming..
   * if in all block.. we find that the db level lower than the THRESHOLD
   * we mute
   *
   * @param pcm_data - 1sec worth of data
   * @return
   */
  private static final double DB_CUTOFF_LVL = -65;

  private boolean shouldMuteTrackNow(byte[] pcm_data, int pcm_len) {
    double level, average_sample = 0, total_sample = 0;
    short sample16bit = 0;

    for (int i = 0; i < pcm_len; i += 2) {
      sample16bit = (short) (pcm_data[i + 1] & 0xFF);
      sample16bit = (short) (sample16bit << 8);
      sample16bit += pcm_data[i] & 0xFF;

      total_sample += Math.abs(sample16bit);
    }

    average_sample = total_sample / (pcm_len / 2);
    level = 20 * Math.log10(Math.abs(average_sample) / 32768);
//		Log.d("mbp", "audio len: " + pcm_len);
//		Log.d("mbp", "for  avg lve: "+ level);
    if (level > DB_CUTOFF_LVL) {
      return false;
    }

    return true;
  }

  private long start_time;

  public long unReadData() {
//		int start = read_next, end = write_next; 
//		int buff_size = 0;
//		int i = start; 
//		
//		while (i != end)
//		{
//			buff_size += s_buffers_len[i];
//			i = (i +1 )%NUM_BUFF;
//		}
		
		/* */
//		int buffer_in_hw = 
//				this.overallDataBuffered  -
//				16000 * ( (int) (System.currentTimeMillis()  - start_time)/1000); 
//		return buffer_in_hw;

//		return buff_size;
    return overallDataBuffered;
  }


  public int isBuffFull() {
    int isFull = 0;

    //if (forPlaybackOrRelay == true)
    {
      long buff_size = unReadData();
      if (buff_size >= NUM_BUFF * maxBufferSize / 2) {
        isFull = 1;
      }
//			Log.d("mbp", "readnext: " + read_next + 
//					" writenext: " + write_next +
//					" unReadData: " + unReadData() +
//					" isBuffFull? " + isFull);
    }


    return isFull;
  }


  private void updateAudioClk(double pts) {
    if (ffmpeg_callback != null) {
      ffmpeg_callback.native_updateAudioClk(pts);
    }
  }


  /**
   * * store the offset of each sub-buffer
   */
  public void writePCM(byte[] pcm, int pcm_len) {
    //int offset = 0;
    int buffer_offset;// = s_buffers_offset[write_next];
    int remaining = pcm_len;
    int pcm_offset = 0;
    int copied_len;
    //Log.d("mbp", "pcm_len: " + pcm_len);
    while (remaining > 0) {
      if (isBuffFull() == 0) {
        if (s_buffer_status[write_next] == BUFFER_EMPTY) {
          buffer_offset = s_buffers_offset[write_next];
          //if( (offset + ONE_BUFF_SIZE) <= pcm_len)
          if ((buffer_offset + remaining) <= (maxBufferSize / 2)) {
            //copied_len = ONE_BUFF_SIZE;
            copied_len = remaining;
            //				Log.d("mbp","PCMPlayer spill over nxt buff pcmlen: " + pcm_len +
            //						" offset: "+ offset + " copied len: " + copied_len);
          } else {
            //copied_len = pcm_len - offset;
            copied_len = (maxBufferSize / 2) - buffer_offset;
            //				Log.d("mbp","PCMPlayer spill over nxt buff pcmlen: " + pcm_len +
            //						" offset: "+ offset + " copied len: " + copied_len);
          }

          s_buffer_status[write_next] = BUFFER_PROCESSING;
          //synchronized(s_buffers)
          {
            remaining -= copied_len;
//						Log.d("mbp","buffer_offset: " + buffer_offset + " copied_len: " + copied_len + " remaining: " + remaining);
            System.arraycopy(pcm, pcm_offset, s_buffers[write_next], buffer_offset, copied_len);
//						if (debug_write_to_file ==true)
//						{
//							//DBG write pcm data to file
//							writeAudioDataToFile(s_buffers[write_next],buffer_offset,copied_len);
//						}
            pcm_offset += copied_len;

            s_buffers_len[write_next] += copied_len;
            try {
              buffer_offset = (buffer_offset + copied_len) % (maxBufferSize / 2);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
//						Log.d("mbp","Buffer empty ..s_buffers_len:" + s_buffers_len[write_next] +
//								" write_next:" + write_next + " offset: " + buffer_offset +
//								" remaining: " + remaining);
            s_buffers_offset[write_next] = buffer_offset;//start reading at 0
//						Log.d("mbp","s_buffers_offset: " + s_buffers_offset[write_next] + " write_next: " + write_next);
            if (s_buffers_len[write_next] == (maxBufferSize / 2)) {
              s_buffer_status[write_next] = BUFFER_FULL;
              write_next = (write_next + 1) % NUM_BUFF;
            } else {
              s_buffer_status[write_next] = BUFFER_EMPTY;
            }

            overallDataBuffered += copied_len;
          }

        } else// s_buffer_status[write_next] != PublicDefine.BUFFER_EMPTY
        {
          //				Log.d("mbp","Buffer busy -- sleep for a 50" +
          //						"write_next= " +write_next +" read_next=" + read_next);
          //wait fot the buffer to be consumed.
          try {
            Thread.sleep(63);
          } catch (InterruptedException e) {
          }
        }
      } else {
        try {
          Thread.sleep(20);
        } catch (InterruptedException e) {
        }
      }

    }

    if (debug_write_to_file == true) {
      //DBG write pcm data to file
      writeAudioDataToFile(pcm, 0, pcm_len);
    }
  }


  /**
   * * store the offset of each sub-buffer
   */
  public void writePCMWithPTS(byte[] pcm, int pcm_len, double pts) {
    /**** store the offset of each sub-buffer */
    int offset = 0;

    while (offset < pcm_len && _isAlive) {
      int copied_len;

      if ((offset + ONE_BUFF_SIZE) <= pcm_len) {
        copied_len = ONE_BUFF_SIZE;
      } else {
        copied_len = pcm_len - offset;
      }


      if (s_buffer_status[write_next] == PublicDefine.BUFFER_EMPTY) {
        s_buffer_status[write_next] = PublicDefine.BUFFER_PROCESSING;
        //synchronized(s_buffers)
        {
          System.arraycopy(pcm, offset, s_buffers[write_next], 0, copied_len);
          s_buffers_len[write_next] = copied_len;
          s_buffers_offset[write_next] = 0;//start reading at 0
          s_buffer_status[write_next] = PublicDefine.BUFFER_FULL;
          s_buffers_pts[write_next] = pts; //store pts to use later
          overallDataBuffered += copied_len;

          offset += copied_len;
          write_next = (write_next + 1) % NUM_BUFF;


        }

      } else// s_buffer_status[write_next] != PublicDefine.BUFFER_EMPTY
      {
        try {
          Thread.sleep(63);
        } catch (InterruptedException e) {
        }
      }

    }


    if (debug_write_to_file == true) {
      //DBG write pcm data to file
      writeAudioDataToFile(pcm, 0, pcm_len);
    }
  }


  public void stop() {
    _isAlive = false;

    if (debug_write_to_file) {
      stopWriteTofile();
    }

  }


  private void initWriteTofile() {
    Log.d("mbp", "pcm file is: " + filename);
    os = null;
    try {
      os = new FileOutputStream(filename);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void stopWriteTofile() {
    Log.d("mbp", "Stop write to file");
    if (os != null) {
      try {
        os.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void writeAudioDataToFile(byte[] pcm, int offset, int pcm_len) {

    if (os == null) {
      Log.e("mbp", "ERROR NO OUTPUT STREAM");
      return;
    }

    try {
      Log.d("mbp", "Write audio data to file: " + pcm_len);
      os.write(pcm, offset, pcm_len);
    } catch (IOException e) {
      Log.e("mbp", "ERROR WHILE WRITING");
      os = null;
    }

  }
}