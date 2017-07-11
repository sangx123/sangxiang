package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetCalledStateEnableRsp extends SeqidPacket { 

	com.liz.cptr.TSetCalledStateEnableResult.Enum result;

	final private static String RESULT = "result";


	private TSetCalledStateEnableRsp() {

	}

	public static class Factory {
		static public TSetCalledStateEnableRsp newInstance() {
			return new TSetCalledStateEnableRsp();
		}
	}


	static public TSetCalledStateEnableRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetCalledStateEnableRsp packet = new TSetCalledStateEnableRsp();
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
		packet.result = com.liz.cptr.TSetCalledStateEnableResult.Enum.parse(parse.takeValue());

		
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
		return com.liz.cptr.CptRPacketParse.SET_CALLED_STATE_ENABLE_RSP;
	}
	public com.liz.cptr.TSetCalledStateEnableResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TSetCalledStateEnableResult.Enum result) {
		this.result = result;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

