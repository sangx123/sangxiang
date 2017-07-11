package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetSingleOptionRsp extends SeqidPacket { 

	com.liz.cptr.TGetSingleOptionResult.Enum result;
	Boolean value = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String VALUE = "value";
	final private static String USER_TYPE = "user_type";


	private TGetSingleOptionRsp() {

	}

	public static class Factory {
		static public TGetSingleOptionRsp newInstance() {
			return new TGetSingleOptionRsp();
		}
	}


	static public TGetSingleOptionRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetSingleOptionRsp packet = new TGetSingleOptionRsp();
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
		packet.result = com.liz.cptr.TGetSingleOptionResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(VALUE)) {
			packet.value = parse.takeValue().toLowerCase().equals("true") ? TRUE : FALSE;
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

		if (value != null) {
			SimpleXmlParse.appendXmlStart(buffer, VALUE);
			buffer.append(value);
			SimpleXmlParse.appendXmlEnd(buffer, VALUE);
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_SINGLE_OPTION_RSP;
	}
	public com.liz.cptr.TGetSingleOptionResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetSingleOptionResult.Enum result) {
		this.result = result;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public boolean isSetValue() {
		return (this.value != null);
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

