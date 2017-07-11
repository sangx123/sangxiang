package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetCalledStateEnableReq extends SeqidPacket { 

	String msisdn;

	final private static String MSISDN = "msisdn";


	private TGetCalledStateEnableReq() {

	}

	public static class Factory {
		static public TGetCalledStateEnableReq newInstance() {
			return new TGetCalledStateEnableReq();
		}
	}


	static public TGetCalledStateEnableReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetCalledStateEnableReq packet = new TGetCalledStateEnableReq();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(MSISDN)) {
			throw new XmlParseException(MSISDN, name);
		}
		packet.msisdn = parse.takeValue();

		
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

		SimpleXmlParse.appendXmlStart(buffer, MSISDN);
		buffer.append(msisdn);
		SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_CALLED_STATE_ENABLE_REQ;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

