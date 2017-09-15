package com.blinkhd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CircularLogFile {
  private static final String TAG = "mbp";
  private long max_size = 3 * 1024;
  private File file = null;
  private String file_path = null;
  private RandomAccessFile log_file = null;
  private long write_idx = 0;
  private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

  public CircularLogFile(String file_path) {
    this.file_path = file_path;
    // Log.d(TAG, "Circular file path: " + file_path);
    this.file = new File(file_path);
  }

  public CircularLogFile(File file) {
    this.file = file;
    if (file != null) {
      this.file_path = file.getAbsolutePath();
    }
    // Log.d(TAG, "Circular file path: " + file_path);
    write_idx = 0;
  }

  public String getPath() {
    return file_path;
  }

  public void open() throws FileNotFoundException, IllegalArgumentException {
    try {
      readWriteLock.writeLock().lock();
      log_file = new RandomAccessFile(file_path, "rw");
      if (write_idx > 0) {
        try {
          log_file.seek(write_idx);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public boolean canWrite() {
    boolean result = false;
    if (log_file != null) {
      result = true;
    }
    return result;
  }

  public void rewind() {
    try {
      readWriteLock.writeLock().lock();
      if (log_file != null) {
        try {
          log_file.seek(0);
          write_idx = 0;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public void close() {
    try {
      readWriteLock.writeLock().lock();

      if (log_file != null) {
        try {
          log_file.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        log_file = null;
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * Get max file size.
   *
   * @return Max file size in kB.
   */
  public long getMaxSize() {
    return max_size;
  }

  /**
   * Set maximum size for log file
   *
   * @param max_size Max file size in kB
   */
  public void setMaxSize(long max_size) {
    this.max_size = max_size;
  }

  /**
   * Writes byteCount bytes from the byte array buffer to this file, starting at the current file pointer and using byteOffset as the first position within buffer to get bytes.
   *
   * @param buffer     the buffer to write.
   * @param byteOffset the index of the first byte in buffer to write.
   * @param byteCount  the number of bytes from the buffer to write.
   * @throws IOException               if an I/O error occurs while writing to this file.
   * @throws IndexOutOfBoundsException if byteCount < 0, byteOffset < 0 or byteCount + byteOffset is greater than the size of buffer.
   */
  public void write(byte[] buffer, int byteOffset, int byteCount) throws FileNotFoundException, IndexOutOfBoundsException {
    try {
      readWriteLock.writeLock().lock();
      if (log_file != null) {
        try {
          long curr_pos = log_file.getFilePointer();
          if (curr_pos + byteCount <= max_size * 1024) {
            log_file.write(buffer, byteOffset, byteCount);
          } else {
            int byte_writes = (int) (max_size * 1024 - curr_pos);
            log_file.write(buffer, byteOffset, byte_writes);
            log_file.seek(0);
            log_file.write(buffer, byteOffset + byte_writes, byteCount - byte_writes);
          }
          write_idx = (write_idx + byteCount) % max_size;
        } catch (IOException e) {
          e.printStackTrace();
          log_file = new RandomAccessFile(file_path, "rw");
          if (write_idx > 0) {
            try {
              log_file.seek(write_idx);
            } catch (IOException e1) {
              e.printStackTrace();
            }
          }
        }
      } else {
        log_file = new RandomAccessFile(file_path, "rw");
        write_idx = 0;
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }
}
