package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSetDurationSceneRsp extends SeqidPacket { 

	com.liz.cptr.TSetDurationSceneResult.Enum result;
	String djPhoneNumber = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String DJ_PHONE_NUMBER = "dj_phone_number";
	final private static String USER_TYPE = "user_type";


	private TSetDurationSceneRsp() {

	}

	public static class Factory {
		static public TSetDurationSceneRsp newInstance() {
			return new TSetDurationSceneRsp();
		}
	}


	static public TSetDurationSceneRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSetDurationSceneRsp packet = new TSetDurationSceneRsp();
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
		packet.result = com.liz.cptr.TSetDurationSceneResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(DJ_PHONE_NUMBER)) {
			packet.djPhoneNumber = parse.takeValue();
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

		if (djPhoneNumber != null) {
			SimpleXmlParse.appendXmlStart(buffer, DJ_PHONE_NUMBER);
			buffer.append(djPhoneNumber);
			SimpleXmlParse.appendXmlEnd(buffer, DJ_PHONE_NUMBER);
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.SET_DURATION_SCENE_RSP;
	}
	public com.liz.cptr.TSetDurationSceneResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TSetDurationSceneResult.Enum result) {
		this.result = result;
	}

	public String getDjPhoneNumber() {
		return djPhoneNumber;
	}

	public void setDjPhoneNumber(String djPhoneNumber) {
		this.djPhoneNumber = djPhoneNumber;
	}

	public boolean isSetDjPhoneNumber() {
		return (this.djPhoneNumber != null);
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

