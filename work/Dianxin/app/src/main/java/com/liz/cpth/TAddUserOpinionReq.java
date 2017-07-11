package com.liz.cpth;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TAddUserOpinionReq extends SeqidPacket { 

	String contactInformation = null;
	String content;
	String msisdn = null;

	final private static String CONTACT_INFORMATION = "contact_information";
	final private static String CONTENT = "content";
	final private static String MSISDN = "msisdn";


	private TAddUserOpinionReq() {

	}

	public static class Factory {
		static public TAddUserOpinionReq newInstance() {
			return new TAddUserOpinionReq();
		}
	}


	static public TAddUserOpinionReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TAddUserOpinionReq packet = new TAddUserOpinionReq();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (name.equals(CONTACT_INFORMATION)) {
			packet.contactInformation = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(CONTENT)) {
			throw new XmlParseException(CONTENT, name);
		}
		packet.content = parse.takeValue();

		name = parse.takeName();
		if (name.equals(MSISDN)) {
			packet.msisdn = parse.takeValue();
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

		if (contactInformation != null) {
			SimpleXmlParse.appendXmlStart(buffer, CONTACT_INFORMATION);
			buffer.append(contactInformation);
			SimpleXmlParse.appendXmlEnd(buffer, CONTACT_INFORMATION);
		}

		SimpleXmlParse.appendXmlStart(buffer, CONTENT);
		buffer.append(content);
		SimpleXmlParse.appendXmlEnd(buffer, CONTENT);

		if (msisdn != null) {
			SimpleXmlParse.appendXmlStart(buffer, MSISDN);
			buffer.append(msisdn);
			SimpleXmlParse.appendXmlEnd(buffer, MSISDN);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cpth.CptHPacketParse.ADD_USER_OPINION_REQ;
	}
	public String getContactInformation() {
		return contactInformation;
	}

	public void setContactInformation(String contactInformation) {
		this.contactInformation = contactInformation;
	}

	public boolean isSetContactInformation() {
		return (this.contactInformation != null);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public boolean isSetMsisdn() {
		return (this.msisdn != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

