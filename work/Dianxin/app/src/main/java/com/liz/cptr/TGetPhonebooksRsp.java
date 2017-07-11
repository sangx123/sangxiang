package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetPhonebooksRsp extends SeqidPacket { 

	com.liz.cptr.TGetPhonebooksResult.Enum result;
	java.util.Vector<com.liz.cptr.TPhonebookReturn> values;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String VALUES = "values";
	final private static String USER_TYPE = "user_type";


	private TGetPhonebooksRsp() {

	}

	public static class Factory {
		static public TGetPhonebooksRsp newInstance() {
			return new TGetPhonebooksRsp();
		}
	}


	static public TGetPhonebooksRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetPhonebooksRsp packet = new TGetPhonebooksRsp();
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
		packet.result = com.liz.cptr.TGetPhonebooksResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		while (name.equals(VALUES)) {
			if (packet.values == null) {
				packet.values = new java.util.Vector<com.liz.cptr.TPhonebookReturn>();
			}
			packet.values.addElement(com.liz.cptr.TPhonebookReturn.parse(parse));
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

		if (values != null) {
			for (int i=0; i<values.size(); i++) {
				com.liz.cptr.TPhonebookReturn o_ = values.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, VALUES);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, VALUES);
			}
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_PHONEBOOKS_RSP;
	}
	public com.liz.cptr.TGetPhonebooksResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetPhonebooksResult.Enum result) {
		this.result = result;
	}

	public java.util.Vector<com.liz.cptr.TPhonebookReturn> getValues() {
		return values;
	}

	public void setValues(java.util.Vector<com.liz.cptr.TPhonebookReturn> values) {
		this.values = values;
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


	public java.util.Vector<com.liz.cptr.TPhonebookReturn> addNewValues() {
		this.values = new java.util.Vector<com.liz.cptr.TPhonebookReturn>();
		return this.values;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

