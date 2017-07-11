package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TDelPhonebooksReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	com.liz.cptr.TBlackwhiteState.Enum state = null;
	String phoneNumber = null;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String STATE = "state";
	final private static String PHONE_NUMBER = "phone_number";


	private TDelPhonebooksReq() {

	}

	public static class Factory {
		static public TDelPhonebooksReq newInstance() {
			return new TDelPhonebooksReq();
		}
	}


	static public TDelPhonebooksReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TDelPhonebooksReq packet = new TDelPhonebooksReq();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (name.equals(CLIENT_TYPE)) {
			packet.clientType = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(MSISDN)) {
			throw new XmlParseException(MSISDN, name);
		}
		packet.msisdn = parse.takeValue();

		name = parse.takeName();
		if (name.equals(STATE)) {
			packet.state = com.liz.cptr.TBlackwhiteState.Enum.parse(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(PHONE_NUMBER)) {
			packet.phoneNumber = parse.takeValue();
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

		if (clientType != null) {
			SimpleXmlParse.appendXmlStart(buffer, CLIENT_TYPE);
			buffer.append(clientType);
			SimpleXmlParse.appendXmlEnd(buffer, CLIENT_TYPE);
		}

		SimpleXmlParse.appendXmlStart(buffer, MSISDN);
		buffer.append(msisdn);
		SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

		if (state != null) {
			SimpleXmlParse.appendXmlStart(buffer, STATE);
			buffer.append(state);
			SimpleXmlParse.appendXmlEnd(buffer, STATE);
		}

		if (phoneNumber != null) {
			SimpleXmlParse.appendXmlStart(buffer, PHONE_NUMBER);
			buffer.append(phoneNumber);
			SimpleXmlParse.appendXmlEnd(buffer, PHONE_NUMBER);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.DEL_PHONEBOOKS_REQ;
	}
	public Integer getClientType() {
		return clientType;
	}

	public void setClientType(Integer clientType) {
		this.clientType = clientType;
	}

	public boolean isSetClientType() {
		return (this.clientType != null);
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public com.liz.cptr.TBlackwhiteState.Enum getState() {
		return state;
	}

	public void setState(com.liz.cptr.TBlackwhiteState.Enum state) {
		this.state = state;
	}

	public boolean isSetState() {
		return (this.state != null);
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



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

