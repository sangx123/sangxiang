package com.msc3;

import java.io.OutputStream;


public class CircularBuffer {
  private int iRead;
  private int iWrite;
  private int data_available;

  private byte[] buffer;
  private int buffer_len;

  public CircularBuffer(int len) {
    buffer = new byte[len];
    buffer_len = len;
    iRead = iWrite = 0;
    data_available = 0;
  }

  public boolean isFull() {
    return (iRead == iWrite) && data_available != 0;
  }

  public boolean isEmpty() {
    return (iRead == iWrite) && data_available == 0;
  }

  public void reset() {
    iRead = iWrite = 0;
    data_available = 0;
  }

  public int write(byte[] data, int data_len) {
    synchronized (buffer) {
      if (isFull()) {
        //// // Log.d("mbp","MBP: circular buffer full!");
        return -1;
      }

			/* writing as much as we can or consider expanding buffer */
      if ((data_available + data_len) > buffer_len) {
        data_len = buffer_len - data_available;
      }

      if ((iWrite + data_len) < buffer_len) {
        System.arraycopy(data, 0, buffer, iWrite, data_len);
        iWrite += data_len;
      } else /* Wrap around Write */ {
        int space_left = buffer_len - iWrite;

        System.arraycopy(data, 0, buffer, iWrite, space_left);

        System.arraycopy(data, space_left, buffer, 0, data_len - space_left);
        iWrite = data_len - space_left;
      }
      data_available += data_len;
    }

    return data_len;
  }


  public int read(byte[] out_data, int num_of_bytes) {
    synchronized (buffer) {
      if (isEmpty()) {
        return -1;
      }


      if (data_available < num_of_bytes) {
        num_of_bytes = data_available;
      }

      if ((iRead + num_of_bytes) < buffer_len) {
        System.arraycopy(buffer, iRead, out_data, 0, num_of_bytes);
        iRead += num_of_bytes;
      } else /* Wrap around Read */ {
        int last_bytes = buffer_len - iRead;
        System.arraycopy(buffer, iRead, out_data, 0, last_bytes);
        System.arraycopy(buffer, 0, out_data, last_bytes, num_of_bytes - last_bytes);
        iRead = num_of_bytes - last_bytes;
      }
      data_available -= num_of_bytes;
    }

    return num_of_bytes;
  }

  public void read(OutputStream os, int num_of_bytes) {
    //TODO
  }

}
