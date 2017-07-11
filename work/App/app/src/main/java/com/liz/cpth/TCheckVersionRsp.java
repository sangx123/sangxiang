package com.liz.cpth;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TCheckVersionRsp extends SeqidPacket { 

	com.liz.cpth.TCheckVersionResult.Enum result;
	String url = null;

	final private static String RESULT = "result";
	final private static String URL = "url";


	private TCheckVersionRsp() {

	}

	public static class Factory {
		static public TCheckVersionRsp newInstance() {
			return new TCheckVersionRsp();
		}
	}


	static public TCheckVersionRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TCheckVersionRsp packet = new TCheckVersionRsp();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(RESULT)) {
			throw new XmlParseException(RESULT, name);
		}
		packet.result = com.liz.cpth.TCheckVersionResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(URL)) {
			packet.url = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		SimpleXmlParse.appendXmlStart(buffer, SEQ_ID);
		buffer.append(seqId);
		SimpleXmlParse.appendXmlEnd(buffer, SEQ_ID);

		SimpleXmlParse.appendXmlStart(buffer, RESULT);
		buffer.append(result);
		SimpleXmlParse.appendXmlEnd(buffer, RESULT);

		if (url != null) {
			SimpleXmlParse.appendXmlStart(buffer, URL);
			buffer.append(url);
			SimpleXmlParse.appendXmlEnd(buffer, URL);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cpth.CptHPacketParse.CHECK_VERSION_RSP;
	}
	public com.liz.cpth.TCheckVersionResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cpth.TCheckVersionResult.Enum result) {
		this.result = result;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isSetUrl() {
		return (this.url != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

