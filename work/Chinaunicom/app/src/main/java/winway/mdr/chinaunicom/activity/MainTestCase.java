/**
 * 
 */
package winway.mdr.chinaunicom.activity;

import java.util.Vector;

import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import android.test.AndroidTestCase;

import com.liz.cpth.TGetSacUrlByMsisdnResult;
import com.liz.cpth.TGetSacUrlByMsisdnRsp;
import com.liz.cpth.TSacServerUrl;
import com.liz.cptr.TUserLoginResult;

/**
 * 
 * @author �Ժ� 
 * E-mail:yuafen821@126.com
 * @version ����ʱ�䣺2012-5-28 ����03:26:38
 * ��˵�� ������ ͨ����������ж�
 */
 
public class MainTestCase extends AndroidTestCase {
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
	  
      public void init(){
    	  TGetSacUrlByMsisdnRsp byMsisdnRsp=dataAccess.getResultByMsis("13067783357");
    	  TGetSacUrlByMsisdnResult.Enum result=byMsisdnRsp.getResult();
    	  if(result==TGetSacUrlByMsisdnResult.SUCCESS){
    		  String url= byMsisdnRsp.getSacServerUrl();
    		  System.out.println("�õ���Url��:"+url);
    		  TUserLoginResult.Enum enum1=dataAccess.loginRequestTest("13067783357", "123456", 	url);
    		  if(enum1==TUserLoginResult.SUCCESS){
    			  System.out.println("��¼�ɹ�");
    		  }
    	  }else if(result==TGetSacUrlByMsisdnResult.FAILED_MSISDN_NOT_FOUND){
    		  Vector<TSacServerUrl> serverUrls=byMsisdnRsp.getServersName();
    		  for (int i = 0; i < serverUrls.size(); i++) {
    			TSacServerUrl sacServerUrl=serverUrls.get(i);
				System.out.println("name----------->>>"+sacServerUrl.getName()+"       url--------->>>>"+sacServerUrl.getSacUrl());
			}
    	  }
      }
}
