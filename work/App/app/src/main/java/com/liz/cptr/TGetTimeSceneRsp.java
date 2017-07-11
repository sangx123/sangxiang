package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetTimeSceneRsp extends SeqidPacket { 

	com.liz.cptr.TGetTimeSceneResult.Enum result;
	java.util.Vector<com.liz.cptr.TTimeSceneData> data = null;
	Integer userType = null;

	final private static String RESULT = "result";
	final private static String DATA = "data";
	final private static String USER_TYPE = "user_type";


	private TGetTimeSceneRsp() {

	}

	public static class Factory {
		static public TGetTimeSceneRsp newInstance() {
			return new TGetTimeSceneRsp();
		}
	}


	static public TGetTimeSceneRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetTimeSceneRsp packet = new TGetTimeSceneRsp();
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
		packet.result = com.liz.cptr.TGetTimeSceneResult.Enum.parse(parse.takeValue());

		name = parse.takeName();
		while (name.equals(DATA)) {
			if (packet.data == null) {
				packet.data = new java.util.Vector<com.liz.cptr.TTimeSceneData>();
			}
			packet.data.addElement(com.liz.cptr.TTimeSceneData.parse(parse));
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

		if (data != null) {
			for (int i=0; i<data.size(); i++) {
				com.liz.cptr.TTimeSceneData o_ = data.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, DATA);
				o_.encode(buffer);
				SimpleXmlParse.appendXmlEnd(buffer, DATA);
			}
		}

		if (userType != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_TYPE);
			buffer.append(userType);
			SimpleXmlParse.appendXmlEnd(buffer, USER_TYPE);
		}

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_TIME_SCENE_RSP;
	}
	public com.liz.cptr.TGetTimeSceneResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetTimeSceneResult.Enum result) {
		this.result = result;
	}

	public java.util.Vector<com.liz.cptr.TTimeSceneData> getData() {
		return data;
	}

	public void setData(java.util.Vector<com.liz.cptr.TTimeSceneData> data) {
		this.data = data;
	}

	public boolean isSetData() {
		return (this.data != null);
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


	public java.util.Vector<com.liz.cptr.TTimeSceneData> addNewData() {
		this.data = new java.util.Vector<com.liz.cptr.TTimeSceneData>();
		return this.data;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

