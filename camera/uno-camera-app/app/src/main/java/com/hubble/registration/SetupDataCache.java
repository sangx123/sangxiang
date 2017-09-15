package com.hubble.registration;

import android.os.Environment;

import com.hubble.registration.models.CamChannel;
import com.hubble.registration.models.LegacyCamProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Deprecated
public class SetupDataCache {

  public static final String SETUP_FILE = "mbp_setup.dat";

  public static final int ACCESS_VIA_INTERNET = 0x02;
  public static final int ACCESS_VIA_LAN = 0x01;
  private static final String TAG = "SetupDataCache";

  private int access_mode;
  private String current_ssid_;
  private CamChannel[] channels;
  private LegacyCamProfile[] configured_cams;

  //private static  Semaphore readWriteLock;
  private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

  /* Used when LOADing data */
  public SetupDataCache() {
    access_mode = -1;
    current_ssid_ = null;
    channels = null;
    configured_cams = null;

    //Only one person can read/write at one time
    //readWriteLock = new Semaphore(1);
  }

  /* Used when SAVEing data */
  public SetupDataCache(int a_mode, String home_ssid, CamChannel[] channs, LegacyCamProfile[] cps) {
    access_mode = a_mode;
    current_ssid_ = home_ssid;
    channels = channs;
    configured_cams = cps;

    //Only one person can read/write at one time
    //readWriteLock = new Semaphore(1);
  }


  public void set_AccessMode(int amode) {
    this.access_mode = amode;
  }

  public int get_AccessMode() {
    return this.access_mode;
  }

  public void set_SSID(String ssid) {
    this.current_ssid_ = ssid;
  }

  public String get_SSID() {
    return this.current_ssid_;
  }

  public void set_Channels(CamChannel[] chann_array) {
    this.channels = chann_array;
  }

  public CamChannel[] get_Channels() {
    return this.channels;
  }


  public void set_CamProfiles(LegacyCamProfile[] cam_profile) {
    this.configured_cams = cam_profile;
  }

  public LegacyCamProfile[] get_CamProfiles() {
    return this.configured_cams;
  }

  private boolean check_data_before_saving() {
    return !((access_mode == -1) || (current_ssid_ == null));
  }


  public void clear_session_data(File externalFileDir) {

//		try {
//			readWriteLock.acquire();
//		} catch (InterruptedException e) {
//			// // Log.e(TAG, Log.getStackTraceString(e));
//		}

    String state = Environment.getExternalStorageState();
    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.e(TAG, "External Storage is mounted as READONLY!");
        return;
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e(TAG, "External Storage is not ready! (mount/unmount)");
        return;
    }

    try {
      readWriteLock.writeLock().lock();
      /* create a data file in the external dir */
      File file = new File(externalFileDir, SETUP_FILE);
      if (file.exists()) {
        // // Log.d(TAG, "Remove offline data");
        /* remove the old file */
        file.delete();
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }

    //readWriteLock.release();
  }

  /**
   * * Save And Restore data function
   */
  public boolean save_session_data(File externalFileDir) {

		/* Check if the External Storage is available and writeable */
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.e(TAG, "External Storage is mounted as READONLY!");
        return false;
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e(TAG, "External Storage is not ready! (mount/unmount)");
        return false;
    }


//		try {
//			readWriteLock.acquire();
//		} catch (InterruptedException e) {
//			// // Log.e(TAG, Log.getStackTraceString(e));
//		}
//		if (check_data_before_saving() == false)
//		{
//			// // Log.e(TAG, "Not enough data to save");
//			readWriteLock.release();
//			return false;
//		}

    if (!check_data_before_saving()) {
      // // Log.e(TAG, "Not enough data to save");
      return false;
    }

    try {
      readWriteLock.writeLock().lock();
			/* create a data file in the external dir */
      File file = new File(externalFileDir, SETUP_FILE);
      if (file.exists()) {
        //// // Log.e(TAG, "File exist, remove it");
				/* remove the old file */
        file.delete();
      }

      try {
        OutputStream os = new FileOutputStream(file);
        ObjectOutputStream obj_out = new ObjectOutputStream(os);

				/* Write Access mode */
        obj_out.writeInt(access_mode);

				/* Write the Homenetwork SSID */
        obj_out.writeByte(current_ssid_.getBytes().length);
        obj_out.write(current_ssid_.getBytes());

				/* Write out the Channel list */
        obj_out.writeByte(channels.length);
        for (CamChannel channel : channels) {
          obj_out.writeObject(channel);
        }

				/* Write out the configured_cams */
        obj_out.writeByte(configured_cams.length);
        for (LegacyCamProfile configured_cam : configured_cams) {
          obj_out.writeObject(configured_cam);
        }

        obj_out.close();
      } catch (FileNotFoundException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      } catch (IOException e) {

        if (file.exists()) {
					/* remove the incomplete file */
          file.delete();
        }
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
    //readWriteLock.release();

    return true;
  }


  public boolean restore_session_data(File externalFileDir) throws Exception {
    boolean isSucceeded = false;
		
		/* Check if the External Storage is available and Readable */
    String state = Environment.getExternalStorageState();
    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.w(TAG, "External Storage is mounted as READONLY!");
        throw new Exception("External Storage is mounted as READONLY!");
			/*since we just want to read , it's ok to proceed*/
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e(TAG, "External Storage is not ready! (mount/unmount)");
        throw new Exception("External Storage is not ready! (mount/unmount)");
        //return false;
    }

		/* Open a data file in the external dir */
    File file = new File(externalFileDir, SETUP_FILE);
    if (!file.exists()) {
      // // Log.d(TAG, "Setup file not exist");
      return false; //* file does not exists
    }

    try {
      readWriteLock.readLock().lock();
      try {
        InputStream is = new FileInputStream(file);
        ObjectInputStream obj_in = new ObjectInputStream(is);

				/* Read Access mode - not used */
        this.access_mode = obj_in.readInt();


        int len = obj_in.readByte();
        byte[] home_ssid = new byte[len];
        obj_in.readFully(home_ssid);
        //reload current_ssid - not used
        current_ssid_ = new String(home_ssid);

        len = obj_in.readByte();

        channels = new CamChannel[len];
        for (int i = 0; i < len; i++) {
          channels[i] = (CamChannel) obj_in.readObject();
          if (channels[i] == null) {
            channels[i] = new CamChannel();
          }
        }

        len = obj_in.readByte();
        configured_cams = new LegacyCamProfile[len];
        for (int i = 0; i < len; i++) {
          configured_cams[i] = (LegacyCamProfile) obj_in.readObject();

        }

        isSucceeded = true;

        obj_in.close();
      } catch (FileNotFoundException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      } catch (ClassNotFoundException | IOException e) {
        if (file.exists()) {
          file.delete();
        }
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    } finally {
      readWriteLock.readLock().unlock();
    }

    //readWriteLock.release();
    return isSucceeded;
  }

  public boolean hasUpdate(File externalFileDir, long lastTimeRead) {
    File file = new File(externalFileDir, SETUP_FILE);
    if (file.exists()) {
      if (file.lastModified() > lastTimeRead) {
        return true;
      }
    }

    return false;
  }

}
