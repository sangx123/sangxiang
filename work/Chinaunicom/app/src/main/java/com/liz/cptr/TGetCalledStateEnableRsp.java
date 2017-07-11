package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetCalledStateEnableRsp extends SeqidPacket { 

	com.liz.cptr.TGetCalledStateEnableResult.Enum result;
	Boolean blacklist = null;
	Boolean whitelist = null;
	Boolean undisturbed = null;

	final private static String RESULT = "result";
	final private static String BLACKLIST = "blacklist";
	final private static String WHITELIST = "whitelist";
	final private static String UNDISTURBED = "undisturbed";


	private TGetCalledStateEnableRsp() {

	}

	public static class Factory {
		static public TGetCalledStateEnableRsp newInstance() {
			return new TGetCalledStateEnableRsp();
		}
	}


	static public TGetCalledStateEnableRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetCalledStateEnableRsp packet = new TGetCalledStateEnableRsp();
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
		packet.result = com.liz.cptr.TGetCalledStateEnableResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(BLACKLIST)) {
			packet.blacklist = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(WHITELIST)) {
			packet.whitelist = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(UNDISTURBED)) {
			packet.undisturbed = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
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

		SimpleXmlParse.appendXmlStart(buffer, RESULT);
		buffer.append(result);
		SimpleXmlParse.appendXmlEnd(buffer, RESULT);

		if (blacklist != null) {
			SimpleXmlParse.appendXmlStart(buffer, BLACKLIST);
			buffer.append(blacklist);
			SimpleXmlParse.appendXmlEnd(buffer, BLACKLIST);
		}

		if (whitelist != null) {
			SimpleXmlParse.appendXmlStart(buffer, WHITELIST);
			buffer.append(whitelist);
			SimpleXmlParse.appendXmlEnd(buffer, WHITELIST);
		}

		if (undisturbed != null) {
			SimpleXmlParse.appendXmlStart(buffer, UNDISTURBED);
			buffer.append(undisturbed);
			SimpleXmlParse.appendXmlEnd(buffer, UNDISTURBED);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_CALLED_STATE_ENABLE_RSP;
	}
	public com.liz.cptr.TGetCalledStateEnableResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetCalledStateEnableResult.Enum result) {
		this.result = result;
	}

	public Boolean getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(Boolean blacklist) {
		this.blacklist = blacklist;
	}

	public boolean isSetBlacklist() {
		return (this.blacklist != null);
	}

	public Boolean getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(Boolean whitelist) {
		this.whitelist = whitelist;
	}

	public boolean isSetWhitelist() {
		return (this.whitelist != null);
	}

	public Boolean getUndisturbed() {
		return undisturbed;
	}

	public void setUndisturbed(Boolean undisturbed) {
		this.undisturbed = undisturbed;
	}

	public boolean isSetUndisturbed() {
		return (this.undisturbed != null);
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

