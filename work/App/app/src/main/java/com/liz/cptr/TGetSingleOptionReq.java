package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetSingleOptionReq extends SeqidPacket { 

	String msisdn;
	int id;

	final private static String MSISDN = "msisdn";
	final private static String ID = "id";


	private TGetSingleOptionReq() {

	}

	public static class Factory {
		static public TGetSingleOptionReq newInstance() {
			return new TGetSingleOptionReq();
		}
	}


	static public TGetSingleOptionReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetSingleOptionReq packet = new TGetSingleOptionReq();
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
		if (!name.equals(ID)) {
			throw new XmlParseException(ID, name);
		}
		packet.id = Integer.parseInt(parse.takeValue());

		
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

		SimpleXmlParse.appendXmlStart(buffer, ID);
		buffer.append(id);
		SimpleXmlParse.appendXmlEnd(buffer, ID);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_SINGLE_OPTION_REQ;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

