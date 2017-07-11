package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetDurationSceneReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	TDurationSceneData data;
	Boolean autoAdded = null;
	Boolean setGsmStatus = null;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String DATA = "data";
	final private static String AUTO_ADDED = "auto_added";
	final private static String SET_GSM_STATUS = "set_gsm_status";


	private TSetDurationSceneReq() {

	}

	public static class Factory {
		static public TSetDurationSceneReq newInstance() {
			return new TSetDurationSceneReq();
		}
	}


	static public TSetDurationSceneReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetDurationSceneReq packet = new TSetDurationSceneReq();
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
		if (!name.equals(DATA)) {
			throw new XmlParseException(DATA, name);
		}
		packet.data = com.liz.cptr.TDurationSceneData.parse(parse);

		name = parse.takeName();
		if (name.equals(AUTO_ADDED)) {
			packet.autoAdded = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(SET_GSM_STATUS)) {
			packet.setGsmStatus = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
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

		SimpleXmlParse.appendXmlStart(buffer, DATA);
		data.encode(buffer);
		SimpleXmlParse.appendXmlEnd(buffer, DATA);

		if (autoAdded != null) {
			SimpleXmlParse.appendXmlStart(buffer, AUTO_ADDED);
			buffer.append(autoAdded);
			SimpleXmlParse.appendXmlEnd(buffer, AUTO_ADDED);
		}

		if (setGsmStatus != null) {
			SimpleXmlParse.appendXmlStart(buffer, SET_GSM_STATUS);
			buffer.append(setGsmStatus);
			SimpleXmlParse.appendXmlEnd(buffer, SET_GSM_STATUS);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_DURATION_SCENE_REQ;
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

	public TDurationSceneData getData() {
		return data;
	}

	public void setData(TDurationSceneData data) {
		this.data = data;
	}

	public Boolean getAutoAdded() {
		return autoAdded;
	}

	public void setAutoAdded(Boolean autoAdded) {
		this.autoAdded = autoAdded;
	}

	public boolean isSetAutoAdded() {
		return (this.autoAdded != null);
	}

	public Boolean getSetGsmStatus() {
		return setGsmStatus;
	}

	public void setSetGsmStatus(Boolean setGsmStatus) {
		this.setGsmStatus = setGsmStatus;
	}

	public boolean isSetSetGsmStatus() {
		return (this.setGsmStatus != null);
	}


	public com.liz.cptr.TDurationSceneData addNewData() {
		this.data = com.liz.cptr.TDurationSceneData.Factory.newInstance();
		return data;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

