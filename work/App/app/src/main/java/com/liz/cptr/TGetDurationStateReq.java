package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetDurationStateReq extends SeqidPacket { 

	String msisdn;
	java.util.Calendar currentTime = null;

	final private static String MSISDN = "msisdn";
	final private static String CURRENT_TIME = "current_time";


	private TGetDurationStateReq() {

	}

	public static class Factory {
		static public TGetDurationStateReq newInstance() {
			return new TGetDurationStateReq();
		}
	}


	static public TGetDurationStateReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetDurationStateReq packet = new TGetDurationStateReq();
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

		name = parse.takeName();
		if (name.equals(CURRENT_TIME)) {
			packet.currentTime = SimpleXmlParse.parseDateTime(parse.takeValue());
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

		SimpleXmlParse.appendXmlStart(buffer, MSISDN);
		buffer.append(msisdn);
		SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

		if (currentTime != null) {
			SimpleXmlParse.appendXmlStart(buffer, CURRENT_TIME);
			SimpleXmlParse.encodeDateTime(currentTime, buffer);
			SimpleXmlParse.appendXmlEnd(buffer, CURRENT_TIME);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_DURATION_STATE_REQ;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public java.util.Calendar getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(java.util.Calendar currentTime) {
		this.currentTime = currentTime;
	}

	public boolean isSetCurrentTime() {
		return (this.currentTime != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

