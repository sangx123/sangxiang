package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetMultipleOptionsReq extends SeqidPacket { 

	String msisdn;
	java.util.Vector<Integer> ids;

	final private static String MSISDN = "msisdn";
	final private static String IDS = "ids";


	private TGetMultipleOptionsReq() {

	}

	public static class Factory {
		static public TGetMultipleOptionsReq newInstance() {
			return new TGetMultipleOptionsReq();
		}
	}


	static public TGetMultipleOptionsReq parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetMultipleOptionsReq packet = new TGetMultipleOptionsReq();
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
		while (name.equals(IDS)) {
			if (packet.ids == null) {
				packet.ids = new java.util.Vector<Integer>();
			}
			packet.ids.addElement(Integer.parseInt(parse.takeValue()));
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

		SimpleXmlParse.appendXmlStart(buffer, MSISDN);
		buffer.append(msisdn);
		SimpleXmlParse.appendXmlEnd(buffer, MSISDN);

		if (ids != null) {
			for (int i=0; i<ids.size(); i++) {
				Integer o_ = ids.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, IDS);
				buffer.append(o_);
				SimpleXmlParse.appendXmlEnd(buffer, IDS);
			}
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_MULTIPLE_OPTIONS_REQ;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public java.util.Vector<Integer> getIds() {
		return ids;
	}

	public void setIds(java.util.Vector<Integer> ids) {
		this.ids = ids;
	}


	public java.util.Vector<Integer> addNewIds() {
		this.ids = new java.util.Vector<Integer>();
		return this.ids;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

