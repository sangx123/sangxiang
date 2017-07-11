package com.liz.cptr;

import lizxsd.parse.SeqidPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TGetSmsInfoRsp extends SeqidPacket { 

	com.liz.cptr.TBlackwhiteState.Enum state = null;
	java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> enabledStates = null;
	com.liz.cptr.TGetSmsInfoResult.Enum result;

	final private static String STATE = "state";
	final private static String ENABLED_STATES = "enabled_states";
	final private static String RESULT = "result";


	private TGetSmsInfoRsp() {

	}

	public static class Factory {
		static public TGetSmsInfoRsp newInstance() {
			return new TGetSmsInfoRsp();
		}
	}


	static public TGetSmsInfoRsp parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TGetSmsInfoRsp packet = new TGetSmsInfoRsp();
		String name;
		
		name = parse.takeName();
		if (!name.equals(SEQ_ID)) {
			throw new XmlParseException(SEQ_ID, name);
		}
		packet.seqId = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (name.equals(STATE)) {
			packet.state = com.liz.cptr.TBlackwhiteState.Enum.parse(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		while (name.equals(ENABLED_STATES)) {
			if (packet.enabledStates == null) {
				packet.enabledStates = new java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum>();
			}
			packet.enabledStates.addElement(com.liz.cptgen.TSmsInfoState.Enum.parse(parse.takeValue()));
			name = parse.takeName();	
		}
		parse.takePreviousName();	

		name = parse.takeName();
		if (!name.equals(RESULT)) {
			throw new XmlParseException(RESULT, name);
		}
		packet.result = com.liz.cptr.TGetSmsInfoResult.Enum.parse(parse.takeValue());

		
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

		if (state != null) {
			SimpleXmlParse.appendXmlStart(buffer, STATE);
			buffer.append(state);
			SimpleXmlParse.appendXmlEnd(buffer, STATE);
		}

		if (enabledStates != null) {
			for (int i=0; i<enabledStates.size(); i++) {
				com.liz.cptgen.TSmsInfoState.Enum o_ = enabledStates.elementAt(i);
				SimpleXmlParse.appendXmlStart(buffer, ENABLED_STATES);
				buffer.append(o_);
				SimpleXmlParse.appendXmlEnd(buffer, ENABLED_STATES);
			}
		}

		SimpleXmlParse.appendXmlStart(buffer, RESULT);
		buffer.append(result);
		SimpleXmlParse.appendXmlEnd(buffer, RESULT);

		
	}
	
	public String getXmlTagName() {
		return com.liz.cptr.CptRPacketParse.GET_SMS_INFO_RSP;
	}
	public com.liz.cptr.TBlackwhiteState.Enum getState() {
		return state;
	}

	public void setState(com.liz.cptr.TBlackwhiteState.Enum state) {
		this.state = state;
	}

	public boolean isSetState() {
		return (this.state != null);
	}

	public java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> getEnabledStates() {
		return enabledStates;
	}

	public void setEnabledStates(java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> enabledStates) {
		this.enabledStates = enabledStates;
	}

	public boolean isSetEnabledStates() {
		return (this.enabledStates != null);
	}

	public com.liz.cptr.TGetSmsInfoResult.Enum getResult() {
		return result;
	}

	public void setResult(com.liz.cptr.TGetSmsInfoResult.Enum result) {
		this.result = result;
	}


	public java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum> addNewEnabledStates() {
		this.enabledStates = new java.util.Vector<com.liz.cptgen.TSmsInfoState.Enum>();
		return this.enabledStates;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

