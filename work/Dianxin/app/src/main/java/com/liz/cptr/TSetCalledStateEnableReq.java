package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetCalledStateEnableReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	com.liz.cptr.TCalledStateEnable.Enum state;
	boolean enabled;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String STATE = "state";
	final private static String ENABLED = "enabled";


	private TSetCalledStateEnableReq() {

	}

	public static class Factory {
		static public TSetCalledStateEnableReq newInstance() {
			return new TSetCalledStateEnableReq();
		}
	}


	static public TSetCalledStateEnableReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetCalledStateEnableReq packet = new TSetCalledStateEnableReq();
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
		if (!name.equals(STATE)) {
			throw new XmlParseException(STATE, name);
		}
		packet.state = com.liz.cptr.TCalledStateEnable.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(ENABLED)) {
			throw new XmlParseException(ENABLED, name);
		}
		packet.enabled = parse.takeValue().toLowerCase().equals("true") ? true : false;

		
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

		SimpleXmlParse.appendXmlStart(buffer, STATE);
		buffer.append(state);
		SimpleXmlParse.appendXmlEnd(buffer, STATE);

		SimpleXmlParse.appendXmlStart(buffer, ENABLED);
		buffer.append(enabled);
		SimpleXmlParse.appendXmlEnd(buffer, ENABLED);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_CALLED_STATE_ENABLE_REQ;
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

	public com.liz.cptr.TCalledStateEnable.Enum getState() {
		return state;
	}

	public void setState(com.liz.cptr.TCalledStateEnable.Enum state) {
		this.state = state;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

