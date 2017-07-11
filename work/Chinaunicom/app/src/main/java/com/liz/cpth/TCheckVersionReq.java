package com.liz.cpth;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TCheckVersionReq extends SeqidPacket { 

	String version;

	final private static String VERSION = "version";


	private TCheckVersionReq() {

	}

	public static class Factory {
		static public TCheckVersionReq newInstance() {
			return new TCheckVersionReq();
		}
	}


	static public TCheckVersionReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TCheckVersionReq packet = new TCheckVersionReq();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(VERSION)) {
			throw new XmlParseException(VERSION, name);
		}
		packet.version = parse.takeValue();

		
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

		SimpleXmlParse.appendXmlStart(buffer, VERSION);
		buffer.append(version);
		SimpleXmlParse.appendXmlEnd(buffer, VERSION);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cpth.CptHPacketParse.CHECK_VERSION_REQ;
	}
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

