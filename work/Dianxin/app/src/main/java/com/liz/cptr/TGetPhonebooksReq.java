package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetPhonebooksReq extends SeqidPacket { 

	String msisdn;
	com.liz.cptr.TBlackwhiteState.Enum state = null;
	String phoneNumber = null;
	Integer offset = null;
	Integer count = null;

	final private static String MSISDN = "msisdn";
	final private static String STATE = "state";
	final private static String PHONE_NUMBER = "phone_number";
	final private static String OFFSET = "offset";
	final private static String COUNT = "count";


	private TGetPhonebooksReq() {

	}

	public static class Factory {
		static public TGetPhonebooksReq newInstance() {
			return new TGetPhonebooksReq();
		}
	}


	static public TGetPhonebooksReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetPhonebooksReq packet = new TGetPhonebooksReq();
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

		name = parse.takeName();
		if (name.equals(OFFSET)) {
			packet.offset = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(COUNT)) {
			packet.count = Integer.valueOf(parse.takeValue());
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

		if (offset != null) {
			SimpleXmlParse.appendXmlStart(buffer, OFFSET);
			buffer.append(offset);
			SimpleXmlParse.appendXmlEnd(buffer, OFFSET);
		}

		if (count != null) {
			SimpleXmlParse.appendXmlStart(buffer, COUNT);
			buffer.append(count);
			SimpleXmlParse.appendXmlEnd(buffer, COUNT);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_PHONEBOOKS_REQ;
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

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public boolean isSetOffset() {
		return (this.offset != null);
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public boolean isSetCount() {
		return (this.count != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

