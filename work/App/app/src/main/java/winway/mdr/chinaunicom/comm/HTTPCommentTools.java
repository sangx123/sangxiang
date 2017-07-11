package winway.mdr.chinaunicom.comm;

import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;


public class HTTPCommentTools {
	static HTTPCommentTools commentTools=new HTTPCommentTools();
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
	  public static HTTPCommentTools getInstance() {
		return commentTools;
	}
	
   @SuppressWarnings("static-access")
public  String GetEcption(String error){
	   if(error.equals(dataAccess.getLastError().HTTP_EXCEPTION.toString()))return "网络连接错误";
	   if(error.equals(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString()))return "超时，需要重新登陆!";
	   if(error.equals(dataAccess.getLastError().HTTP_SYSTEM_ERROR.toString()))return "系统错误";
	   if(error.equals(dataAccess.getLastError().MSISDN_EMPTY.toString()))return "您尚未登录,请先登录!";
	   if(error.equals(dataAccess.getLastError().INPUT_PARAMETER_ERROR.toString()))return "输出的参数错误!";
	   if(error.equals(dataAccess.getLastError().HTTP_SYSTEM_ERROR.toString()))return "访问服务器数据失败，可能是网络忙或者服务器正在维护！";
	   return "未知错误";
   }
}
