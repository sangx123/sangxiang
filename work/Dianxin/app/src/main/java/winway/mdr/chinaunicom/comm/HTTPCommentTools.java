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
	   if(error.equals(dataAccess.getLastError().HTTP_EXCEPTION.toString()))return "�������Ӵ���";
	   if(error.equals(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString()))return "��ʱ����Ҫ���µ�½!";
	   if(error.equals(dataAccess.getLastError().HTTP_SYSTEM_ERROR.toString()))return "ϵͳ����";
	   if(error.equals(dataAccess.getLastError().MSISDN_EMPTY.toString()))return "����δ��¼,���ȵ�¼!";
	   if(error.equals(dataAccess.getLastError().INPUT_PARAMETER_ERROR.toString()))return "����Ĳ�������!";
	   if(error.equals(dataAccess.getLastError().HTTP_SYSTEM_ERROR.toString()))return "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����";
	   return "δ֪����";
   }
}
