package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetSmsInfoRsp extends SeqidPacket { 

	com.liz.cptr.TSetSmsInfoResult.Enum result;

	final private static String RESULT = "result";


	private TSetSmsInfoRsp() {

	}

	public static class Factory {
		static public TSetSmsInfoRsp newInstance() {
			return new TSetSmsInfoRsp();
		}
	}


	static public TSetSmsInfoRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetSmsInfoRsp packet = new TSetSmsInfoRsp();
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
		packet.result = com.liz.cptr.TSetSmsInfoResult.Enum.parse(parse.takeValue());

		
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

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_SMS_INFO_RSP;
	}
	public com.liz.cptr.TSetSmsInfoResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TSetSmsInfoResult.Enum result) {
		this.result = result;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

