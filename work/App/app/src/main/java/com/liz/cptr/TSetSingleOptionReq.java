package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetSingleOptionReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	TOptionPair option;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String OPTION = "option";


	private TSetSingleOptionReq() {

	}

	public static class Factory {
		static public TSetSingleOptionReq newInstance() {
			return new TSetSingleOptionReq();
		}
	}


	static public TSetSingleOptionReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetSingleOptionReq packet = new TSetSingleOptionReq();
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
		if (!name.equals(OPTION)) {
			throw new XmlParseException(OPTION, name);
		}
		packet.option = com.liz.cptr.TOptionPair.parse(parse);

		
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

		SimpleXmlParse.appendXmlStart(buffer, OPTION);
		option.encode(buffer);
		SimpleXmlParse.appendXmlEnd(buffer, OPTION);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_SINGLE_OPTION_REQ;
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

	public TOptionPair getOption() {
		return option;
	}

	public void setOption(TOptionPair option) {
		this.option = option;
	}


	public com.liz.cptr.TOptionPair addNewOption() {
		this.option = com.liz.cptr.TOptionPair.Factory.newInstance();
		return option;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

