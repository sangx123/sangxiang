package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TDelPhonebooksRsp extends SeqidPacket { 

	com.liz.cptr.TDelPhonebooksResult.Enum result;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String USER_TYPE = "user_type";


	private TDelPhonebooksRsp() {

	}

	public static class Factory {
		static public TDelPhonebooksRsp newInstance() {
			return new TDelPhonebooksRsp();
		}
	}


	static public TDelPhonebooksRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TDelPhonebooksRsp packet = new TDelPhonebooksRsp();
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
		packet.result = com.liz.cptr.TDelPhonebooksResult.Enum.parse(parse.takeValue());

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

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.DEL_PHONEBOOKS_RSP;
	}
	public com.liz.cptr.TDelPhonebooksResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TDelPhonebooksResult.Enum result) {
		this.result = result;
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



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

