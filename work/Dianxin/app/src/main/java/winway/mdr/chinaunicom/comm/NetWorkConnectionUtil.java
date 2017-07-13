package winway.mdr.chinaunicom.comm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ��������
 * @date:   2012-2-5
 * @type:   NetWorkConnectionUtil
 *
 */
public class NetWorkConnectionUtil {
	 public static boolean getServerStatus(String path){
		 return true;
			/*try {
				URL url = new URL(path);
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				if(urlConn.getResponseCode()==200)return true;
				else return false;
			} catch (MalformedURLException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		   */
	    }
}
