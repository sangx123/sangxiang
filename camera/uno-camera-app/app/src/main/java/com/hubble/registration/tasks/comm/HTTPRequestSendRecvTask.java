package com.hubble.registration.tasks.comm;

import android.os.AsyncTask;
import android.util.Base64;

import com.hubble.registration.PublicDefine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;


/* Send HTTP request to Device and recieve responses
 * Response is passed back to UI thread 
 * */

public class HTTPRequestSendRecvTask extends AsyncTask<String, Integer, String> {


  private static final String TAG = "HTTPRequestSendRecvTask";

  /**
   * sendRequest_block_for_response
   *
   * @param urls : url
   *             optional - urls[1] : basic authentication user name
   *             urls[2] : basic authentication pass wrd
   * @return
   */
  public static String sendRequest_block_for_response(String... urls) {
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String usr = "";
    String pwd = "";
    if (urls.length == 3) {
      usr = urls[1];
      pwd = urls[2];
    }

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(urls[0]);
      conn = url.openConnection();

      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      conn.setConnectTimeout(5000);

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
      /* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
      }

    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }


    return response;
  }

  /**
   * sendRequest_block_for_response
   *
   * @param urls : url
   *             optional - urls[1] : basic authentication user name
   *             urls[2] : basic authentication pass wrd
   * @return
   */
  public static String sendRequest_block_for_response_with_timeout(String... urls) {
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String usr = "";
    String pwd = "";
    int timeout = 5000; // Default timeout for local is 5s
    if (urls.length >= 3) {
      usr = urls[1];
      pwd = urls[2];
    }

    if (urls.length >= 4) {
      try {
        timeout = Integer.parseInt(urls[3]);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(urls[0]);
      conn = url.openConnection();

      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(timeout);

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
      /* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
      }

    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }


    return response;
  }

  /**
   * Different from the above:
   * - Supply read time value as a string
   * - retry mechanism up to 3 times (10sec)
   *
   * @param urls
   * @return
   */
  public static String sendRequest_block_for_response_1(String... urls) {
    URL url = null;
    HttpURLConnection conn = null;
    DataInputStream inputStream = null;
    String response = null;

    int resCode = -1;
    String usr = "";
    String pwd = "";
    if (urls.length >= 3) {
      usr = urls[1];
      pwd = urls[2];
    }


    int readTimeOut = 10000;
    if (urls.length == 4) {
      try {
        readTimeOut = Integer.parseInt(urls[3]);
      } catch (NumberFormatException nfe) {
        readTimeOut = 10000;
      }

    }


    String usr_pass = String.format("%s:%s", usr, pwd);

    int localRetry = 3;

    do {
      try {
        url = new URL(urls[0]);
        conn = (HttpURLConnection) url.openConnection();

        conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(readTimeOut);

        inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
        resCode = conn.getResponseCode();
        //// // Log.d("mbp", "resCode:" + resCode);

        if (resCode == HttpURLConnection.HTTP_OK) {
          response = inputStream.readLine();
        }


        break;


      } catch (MalformedURLException e) {
        response = null;
        // // Log.e(TAG, Log.getStackTraceString(e));
      } catch (SocketTimeoutException se) {

				/* Android Doc:
         * This exception is thrown when a timeout expired
				 *  on a socket "read" or "accept" operation.
				 *  
				 *  So if read failed, we will try again until we get read OK or a different Exception 
				 *  If server down, we should get SocketException .. 
				 */

        //// // Log.e(TAG, Log.getStackTraceString(se));

        // // Log.d("mbp", "sendRequest_block_for_response_1:" +
        //  "SocketTimeoutException: " + se.getLocalizedMessage() +
        //  "while sending:  : " + url);
        //localRetry = 1;
        //if (read failed) we try again

      } catch (IOException e) {
        response = null;
        // // Log.e(TAG, Log.getStackTraceString(e));
      }


    } while (localRetry-- > 0);


    return response;
  }


  /**
   * @param urls [0] URL
   *             urls [1] msg to post
   * @return
   */
  public static boolean post_message_to_url(String... urls) {
    URL url = null;
    HttpURLConnection conn = null;
    DataInputStream inputStream = null;

    int resCode = -1;


    int readTimeOut = 10000;
    int localRetry = 5;

    boolean status = false;
    String message = urls[1];
    byte[] bytes = message.getBytes();
    do {
      try {
        url = new URL(urls[0]);
        conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestMethod("POST");

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(readTimeOut);


        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        // post the request
        OutputStream out = conn.getOutputStream();
        out.write(bytes);
        out.close();


        resCode = conn.getResponseCode();
        // // Log.d("mbp", "resCode:" + resCode);

        if (resCode == HttpURLConnection.HTTP_OK) {
          status = true;
          break;
        }


        //if not 200 --> Continue


      } catch (MalformedURLException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      } catch (SocketTimeoutException se) {

				/* Android Doc:
<<<<<<< HEAD
         * This exception is thrown when a timeout expired
=======
                 * This exception is thrown when a timeout expired
>>>>>>> 20150910_sonnguyen_release_temp
				 *  on a socket "read" or "accept" operation.
				 *  
				 *  So if read failed, we will try again until we get read OK or a different Exception 
				 *  If server down, we should get SocketException .. 
				 */

        //// // Log.e(TAG, Log.getStackTraceString(se));

        // // Log.d("mbp", "post_message_to_url exception: during read : " + se.getLocalizedMessage());
        localRetry = 1;
        //if (read failed) we try again

      } catch (IOException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      } finally {
        if (conn != null) {
          conn.disconnect();
        }
      }

      //Sleep for a while before retrying
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }


    } while (localRetry-- > 0);


    return status;
  }


  /**
   * try_Authenticate
   * <p/>
   * try to do Basic Authentication to the ip:port with provided
   * user and pwd
   *
   * @param info (ip, port_str,user,pwd))
   * @return true if successfully auth using usr/pass
   * false otherwise
   */
  public static boolean try_Authenticate(String... info) throws SocketException {
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return false;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
    /* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_VERSION;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return false;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
      } else {
        return false;
      }

    } catch (MalformedURLException e) {
      return false;
    } catch (FileNotFoundException fe) {
      return false;
    } catch (SocketException ce) {
      throw ce;
    } catch (SocketTimeoutException ste) {
      throw new SocketException(ste.getLocalizedMessage());
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      return false;
    }


    return true;
  }


  /**
   * get codecs support
   * <p/>
   * try to do get codecs support with
   * user and pwd
   *
   * @param info (ip, port_str,user,pwd))
   * @return the string representation of codecs support
   * (e.g. "-1", "0", "1", "2", "3").
   * null otherwise
   */
  public static String getCodecsSupport(String... info) throws SocketException {
    String result = null;
    final String str_codecs_support = "get_codecs_support: ";
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_CODECS_SUPPORT;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(str_codecs_support)) {
            result = response.substring(str_codecs_support.length());
          }
        }
      }

    } catch (MalformedURLException | FileNotFoundException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketException e) {
      throw new SocketException(e.getLocalizedMessage());
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return result;
  }


  public static String getFirmwareVersion(String... info) throws SocketException {
    String result = null;
    final String str_version = "get_version: ";
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_VERSION;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(20000);
      conn.setReadTimeout(20000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      int res_code = ((HttpURLConnection) conn).getResponseCode();
      if (res_code == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(str_version)) {
            result = response.substring(str_version.length());
            if (result.equalsIgnoreCase("-1")) {
              result = null;
            }
          }
        }
      }

    } catch (MalformedURLException | FileNotFoundException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketException e) {
      throw new SocketException(e.getLocalizedMessage());
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return result;
  }

  public static String getMacAddress(String... info) throws ConnectException {
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_MAC_ADDRESS;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(PublicDefine.GET_MAC_ADDRESS)) {
            response = response.substring(PublicDefine.GET_MAC_ADDRESS.length() + 2);
            if (response.equalsIgnoreCase("-1")) {
              response = null;
            }
          }
        }
      }

    } catch (ConnectException e) {
      throw e;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return response;
  }


  public static String getUdid(String... info) throws ConnectException {
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_UDID;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(20000);
      conn.setReadTimeout(20000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */

      if (contentType != null) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(PublicDefine.GET_UDID)) {
            response = response.substring(PublicDefine.GET_UDID.length() + 2);
            if (response.equalsIgnoreCase("-1")) {
              response = null;
            }
          }
        }
      }

    } catch (ConnectException e) {
      throw e;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return response;
  }


  public static String getMotionArea(String... info) throws SocketException {
    String result = null;
    final String str_motion_area = "get_motion_area: ";
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_MOTION_AREA_CMD;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(str_motion_area)) {
            result = response.substring(str_motion_area.length());
          }
        }
      }

    } catch (MalformedURLException | FileNotFoundException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketException e) {
      throw new SocketException(e.getLocalizedMessage());
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return result;
  }


  public static String getModel(String... info) throws SocketException {
    String result = null;
    final String str_model = "get_model: ";
    URL url = null;
    URLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String response = null;

    String device_ip, device_port, usr, pwd, http_addr;

    if (info.length != 4) {
      return null;
    }


    device_ip = info[0];
    device_port = info[1];
    usr = info[2];
    pwd = info[3];

    if (usr == null) {
      usr = "";
    }
    if (pwd == null) {
      pwd = "";
    }
		/* build a dummy cmd to test username/pass */
    http_addr = "http://" + device_ip + ":" + device_port +
        PublicDefine.HTTP_CMD_PART + PublicDefine.GET_MODEL;

    String usr_pass = String.format("%s:%s", usr, pwd);

    try {
      url = new URL(http_addr);
      conn = url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      if (((HttpURLConnection) conn).getResponseCode() == 401) {
        // // Log.d("mbp", "Auth failed!");
        return null;
      }

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
			/* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
        if (response != null) {
          if (response.startsWith(str_model)) {
            result = response.substring(str_model.length());
            if (result.equalsIgnoreCase("-1")) {
              result = null;
            }
          }
        }
      }

    } catch (MalformedURLException | FileNotFoundException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketException e) {
      throw new SocketException(e.getLocalizedMessage());
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return result;
  }
	


	/* background thread: post the type of request here 
	 * for e.g.: "http://192.168.2.1/?action=command&command=brightness_plus"
	 * */

  protected String doInBackground(String... urls) {

    URL url = null;
    String response = null;
    String contentType = null;
    HttpURLConnection conn = null;
    DataInputStream _inputStream = null;
    //// // Log.d("mbp","num of req: "+ urls.length);

    for (String url1 : urls) {

      try {
        url = new URL(url1);

      } catch (MalformedURLException e) {

        // // Log.e(TAG, Log.getStackTraceString(e));
      }

      try {
        /* send the request to device by open a connection*/
        conn = (HttpURLConnection) url.openConnection();
        _inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
        contentType = conn.getContentType();
				/* make sure the return type is text before using readLine */
        if (contentType.equalsIgnoreCase("text/plain")) {
          response = _inputStream.readLine();
        }
      } catch (Exception ex) {
        //continue;
        // // Log.e(TAG, Log.getStackTraceString(ex));
      }
    }


    return response;
  }

  /* on UI Thread */
  protected void onProgressUpdate(Integer... progress) {
  }


  /* on UI Thread */
  protected void onPostExecute(String result) {
    // // Log.d("mbp-HTTPrequest", "+++response: >" + result + "<");
    //TODO: parse result
  }


}
