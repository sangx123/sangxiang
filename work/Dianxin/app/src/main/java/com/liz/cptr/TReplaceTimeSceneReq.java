package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TReplaceTimeSceneReq extends SeqidPacket { 

	Integer clientType = null;
	String msisdn;
	int index;
	TTimeSceneData data = null;

	final private static String CLIENT_TYPE = "client_type";
	final private static String MSISDN = "msisdn";
	final private static String INDEX = "index";
	final private static String DATA = "data";


	private TReplaceTimeSceneReq() {

	}

	public static class Factory {
		static public TReplaceTimeSceneReq newInstance() {
			return new TReplaceTimeSceneReq();
		}
	}


	static public TReplaceTimeSceneReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TReplaceTimeSceneReq packet = new TReplaceTimeSceneReq();
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
		if (!name.equals(INDEX)) {
			throw new XmlParseException(INDEX, name);
		}
		packet.index = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (name.equals(DATA)) {
			packet.data = com.liz.cptr.TTimeSceneData.parse(parse);
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

		SimpleXmlParse.appendXmlStart(buffer, INDEX);
		buffer.append(index);
		SimpleXmlParse.appendXmlEnd(buffer, INDEX);

		if (data != null) {
			SimpleXmlParse.appendXmlStart(buffer, DATA);
			data.encode(buffer);
			SimpleXmlParse.appendXmlEnd(buffer, DATA);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.REPLACE_TIME_SCENE_REQ;
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public TTimeSceneData getData() {
		return data;
	}

	public void setData(TTimeSceneData data) {
		this.data = data;
	}

	public boolean isSetData() {
		return (this.data != null);
	}


	public com.liz.cptr.TTimeSceneData addNewData() {
		this.data = com.liz.cptr.TTimeSceneData.Factory.newInstance();
		return data;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

