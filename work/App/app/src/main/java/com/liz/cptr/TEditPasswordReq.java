package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TEditPasswordReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	String old_password;
	String new_password;

	final private static String MSISDN = "msisdn";
	final private static String OLD_PASSWORD = "old_password";
	final private static String NEW_PASSWORD="new_password";

	private TEditPasswordReq() {
	}
	public static class Factory {
		static public TEditPasswordReq newInstance() {
			return new TEditPasswordReq();
		}
	}


	static public TEditPasswordReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TEditPasswordReq packet = new TEditPasswordReq();
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
		if (!name.equals(NEW_PASSWORD)) {
			throw new XmlParseException(NEW_PASSWORD, name);
		}
		packet.new_password = parse.takeValue();
		parse.takeName();
		if (!name.equals(OLD_PASSWORD)) {
			throw new XmlParseException(OLD_PASSWORD, name);
		}
		packet.old_password = parse.takeValue();
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

		SimpleXmlParse.appendXmlStart(buffer, NEW_PASSWORD);
		buffer.append(new_password);
		SimpleXmlParse.appendXmlEnd(buffer, NEW_PASSWORD);
		SimpleXmlParse.appendXmlStart(buffer, OLD_PASSWORD);
		buffer.append(old_password);
		SimpleXmlParse.appendXmlEnd(buffer, OLD_PASSWORD);
		
	}
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.CHANGE_PWD_REQ;
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

	public String getPassword() {
		return new_password;
	}

	public void setPassword(String password) {
		this.new_password = password;
	}

	public String getOld_password() {
		return old_password;
	}
	public void setOld_password(String old_password) {
		this.old_password = old_password;
	}


	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

