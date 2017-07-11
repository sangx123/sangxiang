package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetPhonebooksRsp extends SeqidPacket { 

	com.liz.cptr.TSetPhonebooksResult.Enum result;
	com.liz.cptr.TBlackwhiteState.Enum existedType = null;
	String existedPhoneNumber = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String EXISTED_TYPE = "existed_type";
	final private static String EXISTED_PHONE_NUMBER = "existed_phone_number";
	final private static String USER_TYPE = "user_type";


	private TSetPhonebooksRsp() {

	}

	public static class Factory {
		static public TSetPhonebooksRsp newInstance() {
			return new TSetPhonebooksRsp();
		}
	}


	static public TSetPhonebooksRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetPhonebooksRsp packet = new TSetPhonebooksRsp();
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
		packet.result = com.liz.cptr.TSetPhonebooksResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(EXISTED_TYPE)) {
			packet.existedType = com.liz.cptr.TBlackwhiteState.Enum.parse(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(EXISTED_PHONE_NUMBER)) {
			packet.existedPhoneNumber = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

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

		if (existedType != null) {
			SimpleXmlParse.appendXmlStart(buffer, EXISTED_TYPE);
			buffer.append(existedType);
			SimpleXmlParse.appendXmlEnd(buffer, EXISTED_TYPE);
		}

		if (existedPhoneNumber != null) {
			SimpleXmlParse.appendXmlStart(buffer, EXISTED_PHONE_NUMBER);
			buffer.append(existedPhoneNumber);
			SimpleXmlParse.appendXmlEnd(buffer, EXISTED_PHONE_NUMBER);
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_PHONEBOOKS_RSP;
	}
	public com.liz.cptr.TSetPhonebooksResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TSetPhonebooksResult.Enum result) {
		this.result = result;
	}

	public com.liz.cptr.TBlackwhiteState.Enum getExistedType() {
		return existedType;
	}

	public void setExistedType(com.liz.cptr.TBlackwhiteState.Enum existedType) {
		this.existedType = existedType;
	}

	public boolean isSetExistedType() {
		return (this.existedType != null);
	}

	public String getExistedPhoneNumber() {
		return existedPhoneNumber;
	}

	public void setExistedPhoneNumber(String existedPhoneNumber) {
		this.existedPhoneNumber = existedPhoneNumber;
	}

	public boolean isSetExistedPhoneNumber() {
		return (this.existedPhoneNumber != null);
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

