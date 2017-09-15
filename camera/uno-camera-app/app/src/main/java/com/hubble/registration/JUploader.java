package com.hubble.registration;

import android.util.Log;

import com.hubbleconnected.camera.BuildConfig;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.net.ssl.SSLContext;

public class JUploader extends Observable implements Runnable {
  private static final String TAG = "JUploader";
  private static final int TYPE_UPLOAD_BYTE_ARRAY = 1;

  private float mPercent = 0.0f;
  private long mUploadedBytes = 0;
  private long mFileSize = 0;
  private InputStream mInputStream;
  private byte[] dataInBytes;
  private int type;
  private String mFileName;
  private String mServerURL;

  public static final int UPLOADING = 0;
  public static final int COMPLETE = 2;
  public static final int ERROR = 4;
  public static final int TIMEOUT = 6;
  private int mStatus;
  private String filePath;

  public JUploader(String serverURL, String filePath, String fileName) throws FileNotFoundException {
    File file = new File(filePath);
    long fileSize = file.length();
    InputStream inputStream = new FileInputStream(file);

    this.mServerURL = serverURL;
    this.mInputStream = inputStream;
    this.mFileName = fileName;
    this.mFileSize = fileSize;
    this.filePath = filePath;

    this.mStatus = UPLOADING;
    // start upload
    upload();
  }

  private void uploadFirmwareToCamera() {
    boolean isUploadFirmwareOK = false;
    /**
     * Work around on apache httpclients
     */
    SSLContext sslContext = SSLContexts.createSystemDefault();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
        sslContext);
    final CloseableHttpClient httpClient = HttpClients.custom()
        .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
        .setSSLSocketFactory(sslsf).build();
    HttpPost httpPost = new HttpPost(mServerURL);
    Log.i(TAG, "Upload firmware url " + mServerURL);
    this.mStatus = UPLOADING;
    mUploadedBytes = 0;
    notifyObservers();
    try {
      CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
          new CustomMultiPartEntity.ProgressListener() {

            public void transferred(final long num) {
              mUploadedBytes = num;
              stateChanged();
              if(BuildConfig.DEBUG)
                Log.i(TAG, "Percent completed: " + (int) ((num / (float) mFileSize) * 100));
              /*
              we did not support cancel now
              if (mCancelUploadFw) {
                try {
                  httpClient.close();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
              */
            }
          });

      // We use FileBody to transfer an firmware
      File fwFile = new File(filePath);
      if (fwFile.exists()) {
        Log.i(TAG, "OK, firmware file existing.");
      } else {
        Log.i(TAG, "NOT OK, firmware file missing.");
      }
      multipartContent
          .addPart(
              "uploadfile",
              new FileBody(fwFile,
                  ContentType.MULTIPART_FORM_DATA, mFileName));
      mFileSize = multipartContent.getContentLength();
      Log.i(TAG, "Total firmware size " + mFileSize);
      // Send it
      httpPost.setEntity(multipartContent);
      CloseableHttpResponse response = httpClient.execute(httpPost);
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        Log.i(TAG, "Response content length: " + resEntity.getContentLength());
        if(resEntity.getContentLength() > 0) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
          String line = reader.readLine();
          while (line != null) {
            Log.i(TAG, line);
            line = reader.readLine();
          }
          reader.close();
        }
        isUploadFirmwareOK = true;
      }
      resEntity.consumeContent();
    } catch (ClientProtocolException e) {
      if ("0877".equals(modelId)) {
        isUploadFirmwareOK = true;
      } else {
        isUploadFirmwareOK = false;
      }
    } catch (Exception e) {
      isUploadFirmwareOK = false;
      Log.i(TAG, "ERROR HERE?");
      e.printStackTrace();
    }

    if (isUploadFirmwareOK == true) {
      mStatus = COMPLETE;
      stateChanged();
    } else {
      error();
    }
  }

  public JUploader(String serverURL, byte[] data, String fileName, long fileSize) {
    this.mServerURL = serverURL;
    this.dataInBytes = data;
    this.mFileName = fileName;
    this.mFileSize = fileSize;

    this.mStatus = UPLOADING;
    this.type = TYPE_UPLOAD_BYTE_ARRAY;
  }

  private String modelId;

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public float getPercent() {
    mPercent = (float) mUploadedBytes / (float) mFileSize;
    return mPercent * 100;
  }

  private void upload() {
    Thread thread = new Thread(this);
    thread.start();
  }

  // currently use for upload sig file when upgrade fw
  public String uploadFile(HashMap<String, String> otherParams) {

    int serverResponseCode = 0;
    String result = "";

    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";

    if (dataInBytes == null) {
      Log.i(TAG, "Input data is null.");
      return result;
    } else {
      try {
        URL url = new URL(this.mServerURL);

        // Open a HTTP connection to the URL
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        // conn.setRequestProperty("logname", fileName);
        // conn.setFixedLengthStreamingMode(mFileSize);
        dos = new DataOutputStream(conn.getOutputStream());

        if (otherParams != null && !otherParams.isEmpty()) {
          for (Object o : otherParams.entrySet()) {
            Map.Entry mEntry = (Map.Entry) o;
            Log.i(TAG, mEntry.getKey() + " : " + mEntry.getValue());

            dos.writeBytes("Content-Disposition: form-data; name=\"" + mEntry.getKey() + "\"" + lineEnd + lineEnd + mEntry.getValue() + lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
          }
        } else {
          dos.writeBytes(twoHyphens + boundary + lineEnd);
        }

        dos.writeBytes("Content-Disposition: form-data; name=\"uploadfile\"; filename=\"" + this.mFileName + "\"" + lineEnd);
        dos.writeBytes("Content-Type: text/plain" + lineEnd);

        dos.writeBytes(lineEnd);
        stateChanged();

        dos.write(dataInBytes, 0, dataInBytes.length);

        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        Log.i(TAG, this.mServerURL);
        // Responses from the server (code and message)
        serverResponseCode = conn.getResponseCode();

        Log.i(TAG, "Upload log response is : " + serverResponseCode);

        if (serverResponseCode == 200) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          String tmp = reader.readLine();
          while (tmp != null) {
            result += tmp;
            tmp = reader.readLine();
          }
          reader.close();
          Log.i(TAG, "Upload file successfull.");
          Log.i(TAG, "HTML RESULT " + result);

          mStatus = COMPLETE;
          stateChanged();
        } else {
          Log.i(TAG, "Upload file failed.");
        }

        // close the streams //
        dos.flush();
        dos.close();

      } catch (SocketTimeoutException ex) {
        ex.printStackTrace();
        timeOut();
      } catch (MalformedURLException ex) {
        error();
        ex.printStackTrace();

      } catch (java.net.ProtocolException ex) {
        ex.printStackTrace();
        mStatus = COMPLETE;
        stateChanged();
      } catch (Exception e) {
        error();
        e.printStackTrace();
      }
      Log.d(TAG, "serverResponseCode = " + serverResponseCode);
      return result;
    }
  }

  private void error() {
    mStatus = ERROR;
    stateChanged();
  }

  private void timeOut() {
    mStatus = TIMEOUT;
    stateChanged();
  }

  private void stateChanged() {
    setChanged();
    notifyObservers();
  }

  @Override
  public void run() {
    if (type == TYPE_UPLOAD_BYTE_ARRAY) {
      uploadFile(null);
    } else {
      uploadFirmwareToCamera();
    }
  }

  public int getStatus() {
    return mStatus;
  }

  public void setStatus(int status) {
    this.mStatus = status;
  }
}
