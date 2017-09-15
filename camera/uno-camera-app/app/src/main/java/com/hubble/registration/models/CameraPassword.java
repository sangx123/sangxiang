package com.hubble.registration.models;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

// HACK: this class serializes the camera password and saves it to disk
// NOTE: this class expresses the requirement to save a network password for later use
// TODO: refactor this away!
public class CameraPassword implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 4692857438456274957L;

  public static final String CAM_PWD_FILE = "mbp_cpwd.dat";
  public static final String CAM_PWD_INVALID_OR_DEFAULT = "000000";
  public static final int SLOTS = 8;
  private static final String TAG = "CameraPassword";
  //8 Slots
  private static CameraPassword[] passArr = new CameraPassword[SLOTS];


  public static boolean overWriteCamPassword(File externalFileDir, String macId, String user, String pass) throws Exception {
    erasePasswordforCam(externalFileDir, macId);
    return saveCamPassword(externalFileDir, macId, user, pass);
  }

  public static boolean saveCamPassword(File externalFileDir, String macId, String user, String pass) throws Exception {
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.e("mbp", "External Storage is mounted as READONLY!");
        throw new Exception("External Storage is mounted as READONLY!");
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e("mbp", "External Storage is not ready! (mount/unmount)");
        throw new Exception("External Storage is not ready! (mount/unmount)");
    }

		/* Open a data file in the external dir */
    File file = new File(externalFileDir, CAM_PWD_FILE);
    if (!file.exists()) {
      //if file does not exist
      for (int i = 0; i < SLOTS; i++) {
        //Set every entry to EMPTY entry
        passArr[i] = new CameraPassword();
      }
      passArr[0] = new CameraPassword(macId, user, pass);
      return save_pass_data(passArr, new int[]{1}, externalFileDir);
    } else {
      //if file exists, append the new entry
      int[] next_slot_idx = {-1};
      restore_pass_data(passArr, next_slot_idx, externalFileDir);
      if (next_slot_idx[0] == -1) {
        return false;
      }
      //Overwrite the next index slot
      int next_slot_index = next_slot_idx[0];
      passArr[next_slot_index] = new CameraPassword(macId, user, pass);
      next_slot_index = (next_slot_index + 1) % SLOTS;
      next_slot_idx[0] = next_slot_index;
      return save_pass_data(passArr, next_slot_idx, externalFileDir);
    }
  }

  public static String getPasswordforCam(File externalFileDir, String macId) throws Exception {
    if (macId == null) {
      return null;
    }
    int[] next_slot_idx = {-1};
    if (restore_pass_data(passArr, next_slot_idx, externalFileDir)) {
      for (int i = 0; i < SLOTS; i++) {
        if (passArr[i] != null &&
            passArr[i].getId() != null &&
            macId.equalsIgnoreCase(passArr[i].getId())) {
          return passArr[i].cameraPassword;
        }
      }
    }
    return null;
  }

  /**
   * Erase an entry by setting
   * - the MAC id of the entry to NULL
   * - the password to default
   * This way the entry is still in the array but is considered "DEAD", because when
   * getPasswordforCam() search for any cam, it should skip any NULL-mac entry.
   * <p/>
   * On the other hand, when saveCamPassword() saved the next slot index would keep increasing
   * no matter what. Thus, this "Dead" entry will eventually be overwritten.
   *
   * @param externalFileDir
   * @param macId
   * @return True if erase success
   * False if can't read the file location, file does not exist or entry does not exits
   */
  public static boolean erasePasswordforCam(File externalFileDir, String macId) throws Exception {
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.e("mbp", "External Storage is mounted as READONLY!");
        throw new Exception("External Storage is mounted as READONLY!");
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e("mbp", "External Storage is not ready! (mount/unmount)");
        throw new Exception("External Storage is not ready! (mount/unmount)");
    }

		/* Open a data file in the external dir */
    File file = new File(externalFileDir, CAM_PWD_FILE);
    if (!file.exists()) {
      return false; // measn erase failed : entry does not exist or file does not exists

    } else {
      //if file exists, append the new entry
      int[] next_slot_idx = {-1};
      restore_pass_data(passArr, next_slot_idx, externalFileDir);
      boolean found = false;
      for (int i = 0; i < SLOTS; i++) {
        if (passArr[i] != null &&
            passArr[i].getId() != null &&
            macId.equalsIgnoreCase(passArr[i].getId())) {
          found = true;
          //ERASE here .
          passArr[i].setPass(CAM_PWD_INVALID_OR_DEFAULT);
          passArr[i].setId(null);
          break;
        }
      }
      if (found) {
        return save_pass_data(passArr, next_slot_idx, externalFileDir);
      }
      return false;
    }

  }


  /**
   * @param result          - has to have 8 slots , can do with empty password
   * @param next_slot_idx
   * @param externalFileDir
   * @return
   */
  private static boolean save_pass_data(CameraPassword[] result, int[] next_slot_idx, File externalFileDir) throws Exception {
    if (result == null || next_slot_idx == null) {
      return false;
    }

		/* Check if the External Storage is available and writeable */
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.e("mbp", "External Storage is mounted as READONLY!");
        throw new Exception("External Storage is mounted as READONLY!");
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e("mbp", "External Storage is not ready! (mount/unmount)");
        throw new Exception("External Storage is not ready! (mount/unmount)");
    }

		/* create a data file in the external dir */
    File file = new File(externalFileDir, CAM_PWD_FILE);
    if (file.exists()) {
      // // Log.e("mbp", "File exist, remove it");
      /* remove the old file */
      file.delete();
    }
    try {
      OutputStream os = new FileOutputStream(file);
      ObjectOutputStream obj_out = new ObjectOutputStream(os);
      obj_out.writeInt(next_slot_idx[0]);
      /* Write out the Channel list */
      for (int i = 0; i < SLOTS; i++) {
        obj_out.writeObject(result[i]);
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
    return true;
  }

  /**
   * @param result          - CameraPassword arry with 8 elements, could be empty element
   * @param next_slot_idx   - int array with 1 element, to store the next slot index read from the password file
   *                        use array to read back the index @ caller
   * @param externalFileDir
   * @return
   */
  private static boolean restore_pass_data(CameraPassword[] result, int[] next_slot_idx, File externalFileDir) throws Exception {
    if (result == null || next_slot_idx == null) {
      return false;
    }
		/* Check if the External Storage is available and Readable */
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        // // Log.w("mbp", "External Storage is mounted as READONLY!");
        throw new Exception("External Storage is mounted as READONLY!");
			/*since we just want to read , it's ok to proceed*/
      default:
        // Something else is wrong. It may be one of many other states, but all we need
        //  to know is we can neither read nor write
        // // Log.e("mbp", "External Storage is not ready! (mount/unmount)");
        throw new Exception("External Storage is not ready! (mount/unmount)");
    }

		/* Open a data file in the external dir */
    File file = new File(externalFileDir, CAM_PWD_FILE);
    if (!file.exists()) {
      return false; //* file does not exists
    }

    try {
      InputStream is = new FileInputStream(file);
      ObjectInputStream obj_in = new ObjectInputStream(is);
      next_slot_idx[0] = obj_in.readInt();
      for (int i = 0; i < SLOTS; i++) {
        result[i] = (CameraPassword) obj_in.readObject();
      }
      obj_in.close();
    } catch (FileNotFoundException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (ClassNotFoundException | IOException e) {
      if (file.exists()) {
        file.delete();
      }
      // // Log.e(TAG, Log.getStackTraceString(e));
    }
    return true;
  }

  private String cameraUser;
  private String cameraMacID;
  private String cameraPassword;

  public CameraPassword() {
    cameraMacID = null;
    cameraPassword = null;
    cameraUser = null;
  }

  public CameraPassword(String id, String user, String pass) {
    cameraMacID = id;
    cameraPassword = pass;
    cameraUser = user;
  }

  public void setId(String id) {
    cameraMacID = id;
  }

  public void setPass(String pass) {
    cameraPassword = pass;
  }

  public String getId() {
    return cameraMacID;
  }

  public String getPass() {
    return cameraPassword;
  }

  public String getUser() {
    return cameraUser;
  }
}
