package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetSmsInfoReq extends SeqidPacket { 

	String msisdn;
	String phoneNumber = null;
	java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> state = null;

	final private static String MSISDN = "msisdn";
	final private static String PHONE_NUMBER = "phone_number";
	final private static String STATE = "state";


	private TGetSmsInfoReq() {

	}

	public static class Factory {
		static public TGetSmsInfoReq newInstance() {
			return new TGetSmsInfoReq();
		}
	}


	static public TGetSmsInfoReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetSmsInfoReq packet = new TGetSmsInfoReq();
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
		if (name.equals(PHONE_NUMBER)) {
			packet.phoneNumber = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		while (name.equals(STATE)) {
			if (packet.state == null) {
				packet.state = new java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum>();
			}
			packet.state.addElement(com.liz.cptgen.TSmsInfoState.Enum.parse(parse.takeValue()));
			name = parse.takeName();	
		}
		parse.takePreviousName();	

		
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

		if (phoneNumber != null) {
			SimpleXmlParse.appendXmlStart(buffer, PHONE_NUMBER);
			buffer.append(phoneNumber);
			SimpleXmlParse.appendXmlEnd(buffer, PHONE_NUMBER);
		}

		if (state != null) {
			for (int i=0; i<state.size(); i++) {
				com.liz.cptgen.TSmsInfoState.Enum o_ = state.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, STATE);
				buffer.append(o_);
				SimpleXmlParse.appendXmlEnd(buffer, STATE);
			}
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_SMS_INFO_REQ;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isSetPhoneNumber() {
		return (this.phoneNumber != null);
	}

	public java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> getState() {
		return state;
	}

	public void setState(java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> state) {
		this.state = state;
	}

	public boolean isSetState() {
		return (this.state != null);
	}


	public java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> addNewState() {
		this.state = new java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum>();
		return this.state;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

