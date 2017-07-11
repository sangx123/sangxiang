package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetDurationStateRsp extends SeqidPacket { 

	com.liz.cptr.TGetDurationStateResult.Enum result;
	TDurationSceneData data = null;
	Integer leftSeconds = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String DATA = "data";
	final private static String LEFT_SECONDS = "left_seconds";
	final private static String USER_TYPE = "user_type";


	private TGetDurationStateRsp() {

	}

	public static class Factory {
		static public TGetDurationStateRsp newInstance() {
			return new TGetDurationStateRsp();
		}
	}


	static public TGetDurationStateRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetDurationStateRsp packet = new TGetDurationStateRsp();
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
		packet.result = com.liz.cptr.TGetDurationStateResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (name.equals(DATA)) {
			packet.data = com.liz.cptr.TDurationSceneData.parse(parse);
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(LEFT_SECONDS)) {
			packet.leftSeconds = Integer.valueOf(parse.takeValue());
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

		if (data != null) {
			SimpleXmlParse.appendXmlStart(buffer, DATA);
			data.encode(buffer);
			SimpleXmlParse.appendXmlEnd(buffer, DATA);
		}

		if (leftSeconds != null) {
			SimpleXmlParse.appendXmlStart(buffer, LEFT_SECONDS);
			buffer.append(leftSeconds);
			SimpleXmlParse.appendXmlEnd(buffer, LEFT_SECONDS);
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_DURATION_STATE_RSP;
	}
	public com.liz.cptr.TGetDurationStateResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetDurationStateResult.Enum result) {
		this.result = result;
	}

	public TDurationSceneData getData() {
		return data;
	}

	public void setData(TDurationSceneData data) {
		this.data = data;
	}

	public boolean isSetData() {
		return (this.data != null);
	}

	public Integer getLeftSeconds() {
		return leftSeconds;
	}

	public void setLeftSeconds(Integer leftSeconds) {
		this.leftSeconds = leftSeconds;
	}

	public boolean isSetLeftSeconds() {
		return (this.leftSeconds != null);
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


	public com.liz.cptr.TDurationSceneData addNewData() {
		this.data = com.liz.cptr.TDurationSceneData.Factory.newInstance();
		return data;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

