package winway.mdr.chinaunicom.internet.data.tools;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
public class ParseLoginResultXML extends DefaultHandler {
	
	 
	String tagName;
	String seqid="";
	public static String result="";
	/**
	 * 开始解析
	 * */
	
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	 
	}
	/**
	 * 解析XML中的项
	 * */
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		tagName = localName;
	}
	
	/**
	 * 结束解析XML中的项
	 * */
	
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		tagName = "";
	}

	/***
	 * 读取项中的内容
	 * */
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {
        String reString=new String(ch, start, length);
        if("seqid".equals(tagName)){
        	seqid=reString;
        }else if("result".equals(tagName)){
        	result=reString;
        }
		super.characters(ch, start, length);
	}

	/**
	 * 结束解析
	 * */
	
	public void endDocument() throws SAXException {
		super.endDocument();
	
	}

}

