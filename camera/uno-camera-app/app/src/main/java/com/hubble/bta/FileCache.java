package com.hubble.bta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Created by songn_000 on 12 Aug 2016.
 */
public class FileCache {

  public static void save(File file, Object obj) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(obj);
    oos.flush();
    oos.close();
  }

  /**
   * Get cached data from file
   *
   * @param file file
   * @return cached data, null if error or not found
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static Object get(File file) {
    Object val = null;
    try {
      FileInputStream fis = new FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fis);
      val = ois.readObject();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return val;
  }

  public static void save(String filePath, Object obj) throws IOException {
    save(new File(filePath), obj);
  }

  /**
   * Get cached data from file
   *
   * @param path file path
   * @return cached data, null if error or not found
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static Object get(String path) {
    return get(new File(path));
  }
}
