package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetSingleOptionRsp extends SeqidPacket { 

	com.liz.cptr.TSetSingleOptionResult.Enum result;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String USER_TYPE = "user_type";


	private TSetSingleOptionRsp() {

	}

	public static class Factory {
		static public TSetSingleOptionRsp newInstance() {
			return new TSetSingleOptionRsp();
		}
	}


	static public TSetSingleOptionRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetSingleOptionRsp packet = new TSetSingleOptionRsp();
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
		packet.result = com.liz.cptr.TSetSingleOptionResult.Enum.parse(parse.takeValue());

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
		return com.liz.cptr.CptRPacketParse.SET_SINGLE_OPTION_RSP;
	}
	public com.liz.cptr.TSetSingleOptionResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TSetSingleOptionResult.Enum result) {
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

