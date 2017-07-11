package com.liz.cpth;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TAddUserOpinionRsp extends SeqidPacket { 

	com.liz.cpth.TAddUserOpinionResult.Enum result;

	final private static String RESULT = "result";


	private TAddUserOpinionRsp() {

	}

	public static class Factory {
		static public TAddUserOpinionRsp newInstance() {
			return new TAddUserOpinionRsp();
		}
	}


	static public TAddUserOpinionRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TAddUserOpinionRsp packet = new TAddUserOpinionRsp();
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
		packet.result = com.liz.cpth.TAddUserOpinionResult.Enum.parse(parse.takeValue());

		
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
		return com.liz.cpth.CptHPacketParse.ADD_USER_OPINION_RSP;
	}
	public com.liz.cpth.TAddUserOpinionResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cpth.TAddUserOpinionResult.Enum result) {
		this.result = result;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

