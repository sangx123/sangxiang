package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetMultipleOptionsReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	java.util.Vector<com.liz.cptr.TOptionPair> options;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String OPTIONS = "options";


	private TSetMultipleOptionsReq() {

	}

	public static class Factory {
		static public TSetMultipleOptionsReq newInstance() {
			return new TSetMultipleOptionsReq();
		}
	}


	static public TSetMultipleOptionsReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetMultipleOptionsReq packet = new TSetMultipleOptionsReq();
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
		while (name.equals(OPTIONS)) {
			if (packet.options == null) {
				packet.options = new java.util.Vector<com.liz.cptr.TOptionPair>();
			}
			packet.options.addElement(com.liz.cptr.TOptionPair.parse(parse));
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

		if (clientType != null) {
			SimpleXmlParse.appendXmlStart(buffer, CLIENT_TYPE);
			buffer.append(clientType);
			SimpleXmlParse.appendXmlEnd(buffer, CLIENT_TYPE);
		}

		SimpleXmlParse.appendXmlStart(buffer, MSISDN);
		buffer.append(msisdn);
		SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

		if (options != null) {
			for (int i=0; i<options.size(); i++) {
				com.liz.cptr.TOptionPair o_ = options.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, OPTIONS);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, OPTIONS);
			}
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_MULTIPLE_OPTIONS_REQ;
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

	public java.util.Vector<com.liz.cptr.TOptionPair> getOptions() {
		return options;
	}

	public void setOptions(java.util.Vector<com.liz.cptr.TOptionPair> options) {
		this.options = options;
	}


	public java.util.Vector<com.liz.cptr.TOptionPair> addNewOptions() {
		this.options = new java.util.Vector<com.liz.cptr.TOptionPair>();
		return this.options;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

