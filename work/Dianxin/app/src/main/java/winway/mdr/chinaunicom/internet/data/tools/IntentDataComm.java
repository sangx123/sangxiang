package winway.mdr.chinaunicom.internet.data.tools;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class IntentDataComm {
	   /*****************************************************************
	    * �������� :Request_login  
	    * �� ��˵�� :String seqid,String msisdn,String password �ֻ����� �ֻ�ҵ������
	    * ʱ         �� :2011-11
	    * ����ֵ:String
	    * ����˵��:�����û����ֻ������ҵ��������������ж��Ƿ���֤�ɹ� ���ж�������״̬
	    ****************************************************************/
	public String Request_login(String seqid,String msisdn,String password)
	{
		  try {
			  HttpResponse httpResponse = null;
			  HttpPost httppost = new HttpPost("http://ms.zj165.com/sac/android.r"); 
			  httppost.setEntity(new StringEntity(GetRequest_String(seqid,msisdn,password)));
			  httpResponse = new DefaultHttpClient().execute(httppost);
			  HttpEntity httpEntity = httpResponse.getEntity();
			  InputStream inputStream=httpEntity.getContent();
			  parseXml(new ParseLoginResultXML(), new InputSource(inputStream));
			
		} catch (Exception e) {
			e.printStackTrace();
		}  
		  return  ParseLoginResultXML.result;
	}
	   /*****************************************************************
	    * �������� :parseXml  
	    * �� ��˵�� :DefaultHandler  handler,InputSource inputsource ������Handle
	    * ʱ         �� :2011-11
	    * ����ֵ:��
	    * ����˵��:�����������������
	    ****************************************************************/
	public void parseXml(DefaultHandler  handler,InputSource inputsource){
		 try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParse = factory.newSAXParser();
				XMLReader reader = saxParse.getXMLReader();
				reader.setContentHandler(handler);
				reader.parse(inputsource);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	  /*****************************************************************
	    * �������� :GetRequest_String  
	    * �� ��˵�� :String seqid,String msisdn,String password �ֻ������ҵ������
	    * ʱ         �� :2011-11
	    * ����ֵ:��
	    * ����˵��:ƴ������ʱ����ַ���(XML�ļ�)
	    ****************************************************************/
	public String GetRequest_String(String seqid,String msisdn,String password) {
		 String result="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		 		     "<cptr:cpt_r xmlns:cptr=\"http://www.liz.com/cptr\">" +
		 		     "<user_login_req><seqid>"+seqid+"</seqid><msisdn>"+msisdn+"</msisdn>" +
		 				"<password>"+password+"</password></user_login_req></cptr:cpt_r>";
		return result;
	}
}
