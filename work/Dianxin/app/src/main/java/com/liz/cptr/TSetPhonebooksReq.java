package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetPhonebooksReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	String originalNumber = null;
	Boolean forceUpdate = null;
	TPhonebookReturn info;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String ORIGINAL_NUMBER = "original_number";
	final private static String FORCE_UPDATE = "force_update";
	final private static String INFO = "info";


	private TSetPhonebooksReq() {

	}

	public static class Factory {
		static public TSetPhonebooksReq newInstance() {
			return new TSetPhonebooksReq();
		}
	}


	static public TSetPhonebooksReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetPhonebooksReq packet = new TSetPhonebooksReq();
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
		if (name.equals(ORIGINAL_NUMBER)) {
			packet.originalNumber = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(FORCE_UPDATE)) {
			packet.forceUpdate = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(INFO)) {
			throw new XmlParseException(INFO, name);
		}
		packet.info = com.liz.cptr.TPhonebookReturn.parse(parse);

		
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

		if (originalNumber != null) {
			SimpleXmlParse.appendXmlStart(buffer, ORIGINAL_NUMBER);
			buffer.append(originalNumber);
			SimpleXmlParse.appendXmlEnd(buffer, ORIGINAL_NUMBER);
		}

		if (forceUpdate != null) {
			SimpleXmlParse.appendXmlStart(buffer, FORCE_UPDATE);
			buffer.append(forceUpdate);
			SimpleXmlParse.appendXmlEnd(buffer, FORCE_UPDATE);
		}

		SimpleXmlParse.appendXmlStart(buffer, INFO);
		info.encode(buffer);
		SimpleXmlParse.appendXmlEnd(buffer, INFO);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_PHONEBOOKS_REQ;
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

	public String getOriginalNumber() {
		return originalNumber;
	}

	public void setOriginalNumber(String originalNumber) {
		this.originalNumber = originalNumber;
	}

	public boolean isSetOriginalNumber() {
		return (this.originalNumber != null);
	}

	public Boolean getForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(Boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public boolean isSetForceUpdate() {
		return (this.forceUpdate != null);
	}

	public TPhonebookReturn getInfo() {
		return info;
	}

	public void setInfo(TPhonebookReturn info) {
		this.info = info;
	}


	public com.liz.cptr.TPhonebookReturn addNewInfo() {
		this.info = com.liz.cptr.TPhonebookReturn.Factory.newInstance();
		return info;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

