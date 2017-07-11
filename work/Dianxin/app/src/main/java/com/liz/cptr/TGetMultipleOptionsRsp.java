package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetMultipleOptionsRsp extends SeqidPacket { 

	com.liz.cptr.TGetMultipleOptionsResult.Enum result;
	java.util.Vector<com.liz.cptr.TOptionPair> options = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String OPTIONS = "options";
	final private static String USER_TYPE = "user_type";


	private TGetMultipleOptionsRsp() {

	}

	public static class Factory {
		static public TGetMultipleOptionsRsp newInstance() {
			return new TGetMultipleOptionsRsp();
		}
	}


	static public TGetMultipleOptionsRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetMultipleOptionsRsp packet = new TGetMultipleOptionsRsp();
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
		packet.result = com.liz.cptr.TGetMultipleOptionsResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		while (name.equals(OPTIONS)) {
			if (packet.options == null) {
				packet.options = new java.util.Vector<com.liz.cptr.TOptionPair>();
			}
			packet.options.addElement(com.liz.cptr.TOptionPair.parse(parse));
			name = parse.takeName();	
		}
		parse.takePreviousName();	

		name = parse.takeName();
		if (name.equals(USER_TYPE)) {
			packet.userType = Integer.valueOf(parse.takeValue());
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

		if (options != null) {
			for (int i=0; i<options.size(); i++) {
				com.liz.cptr.TOptionPair o_ = options.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, OPTIONS);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, OPTIONS);
			}
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_MULTIPLE_OPTIONS_RSP;
	}
	public com.liz.cptr.TGetMultipleOptionsResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetMultipleOptionsResult.Enum result) {
		this.result = result;
	}

	public java.util.Vector<com.liz.cptr.TOptionPair> getOptions() {
		return options;
	}

	public void setOptions(java.util.Vector<com.liz.cptr.TOptionPair> options) {
		this.options = options;
	}

	public boolean isSetOptions() {
		return (this.options != null);
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public boolean isSetUserType() {
		return (this.userType != null);
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

