package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetSmsInfoReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	java.util.Vector<com.liz.cptr.TSmsInfoData> state;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String STATE = "state";


	private TSetSmsInfoReq() {

	}

	public static class Factory {
		static public TSetSmsInfoReq newInstance() {
			return new TSetSmsInfoReq();
		}
	}


	static public TSetSmsInfoReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetSmsInfoReq packet = new TSetSmsInfoReq();
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
		while (name.equals(STATE)) {
			if (packet.state == null) {
				packet.state = new java.util.Vector<com.liz.cptr.TSmsInfoData>();
			}
			packet.state.addElement(com.liz.cptr.TSmsInfoData.parse(parse));
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

		if (state != null) {
			for (int i=0; i<state.size(); i++) {
				com.liz.cptr.TSmsInfoData o_ = state.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, STATE);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, STATE);
			}
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_SMS_INFO_REQ;
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

	public java.util.Vector<com.liz.cptr.TSmsInfoData> getState() {
		return state;
	}

	public void setState(java.util.Vector<com.liz.cptr.TSmsInfoData> state) {
		this.state = state;
	}


	public java.util.Vector<com.liz.cptr.TSmsInfoData> addNewState() {
		this.state = new java.util.Vector<com.liz.cptr.TSmsInfoData>();
		return this.state;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

