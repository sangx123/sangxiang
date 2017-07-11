package com.liz.cpth;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetSacUrlByMsisdnRsp extends SeqidPacket { 

	com.liz.cpth.TGetSacUrlByMsisdnResult.Enum result;
	String sacServerUrl = null;
	java.util.Vector<com.liz.cpth.TSacServerUrl> serversName = null;

	final private static String RESULT = "result";
	final private static String SAC_SERVER_URL = "sac_server_url";
	final private static String SERVERS_NAME = "servers_name";


	private TGetSacUrlByMsisdnRsp() {

	}

	public static class Factory {
		static public TGetSacUrlByMsisdnRsp newInstance() {
			return new TGetSacUrlByMsisdnRsp();
		}
	}


	static public TGetSacUrlByMsisdnRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetSacUrlByMsisdnRsp packet = new TGetSacUrlByMsisdnRsp();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(RESULT)) {
			throw new XmlParseException(RESULT, name);
		}
		packet.result = com.liz.cpth.TGetSacUrlByMsisdnResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(SAC_SERVER_URL)) {
			packet.sacServerUrl = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		while (name.equals(SERVERS_NAME)) {
			if (packet.serversName == null) {
				packet.serversName = new java.util.Vector<com.liz.cpth.TSacServerUrl>();
			}
			packet.serversName.addElement(com.liz.cpth.TSacServerUrl.parse(parse));
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

		SimpleXmlParse.appendXmlStart(buffer, RESULT);
		buffer.append(result);
		SimpleXmlParse.appendXmlEnd(buffer, RESULT);

		if (sacServerUrl != null) {
			SimpleXmlParse.appendXmlStart(buffer, SAC_SERVER_URL);
			buffer.append(sacServerUrl);
			SimpleXmlParse.appendXmlEnd(buffer, SAC_SERVER_URL);
		}

		if (serversName != null) {
			for (int i=0; i<serversName.size(); i++) {
				com.liz.cpth.TSacServerUrl o_ = serversName.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, SERVERS_NAME);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, SERVERS_NAME);
			}
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cpth.CptHPacketParse.GET_SAC_URL_BY_MSISDN_RSP;
	}
	public com.liz.cpth.TGetSacUrlByMsisdnResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cpth.TGetSacUrlByMsisdnResult.Enum result) {
		this.result = result;
	}

	public String getSacServerUrl() {
		return sacServerUrl;
	}

	public void setSacServerUrl(String sacServerUrl) {
		this.sacServerUrl = sacServerUrl;
	}

	public boolean isSetSacServerUrl() {
		return (this.sacServerUrl != null);
	}

	public java.util.Vector<com.liz.cpth.TSacServerUrl> getServersName() {
		return serversName;
	}

	public void setServersName(java.util.Vector<com.liz.cpth.TSacServerUrl> serversName) {
		this.serversName = serversName;
	}

	public boolean isSetServersName() {
		return (this.serversName != null);
	}


	public java.util.Vector<com.liz.cpth.TSacServerUrl> addNewServersName() {
		this.serversName = new java.util.Vector<com.liz.cpth.TSacServerUrl>();
		return this.serversName;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

