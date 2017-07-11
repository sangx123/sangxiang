package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSmsInfoData implements IPacket { 

	com.liz.cptgen.TSmsInfoState.Enum type;
	boolean enabled;

	final private static String TYPE = "type";
	final private static String ENABLED = "enabled";


	private TSmsInfoData() {

	}

	public static class Factory {
		static public TSmsInfoData newInstance() {
			return new TSmsInfoData();
		}
	}


	static public TSmsInfoData parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSmsInfoData packet = new TSmsInfoData();
		String name;
		
		name = parse.takeName();
		if (!name.equals(TYPE)) {
			throw new XmlParseException(TYPE, name);
		}
		packet.type = com.liz.cptgen.TSmsInfoState.Enum.parse(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(ENABLED)) {
			throw new XmlParseException(ENABLED, name);
		}
		packet.enabled = parse.takeValue().toLowerCase().equals("true") ? true : false;

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		SimpleXmlParse.appendXmlStart(buffer, TYPE);
		buffer.append(type);
		SimpleXmlParse.appendXmlEnd(buffer, TYPE);

		SimpleXmlParse.appendXmlStart(buffer, ENABLED);
		buffer.append(enabled);
		SimpleXmlParse.appendXmlEnd(buffer, ENABLED);

		
	}
	
	public String getXmlTagName() {
		return "t_sms_info_data";
	}
	public com.liz.cptgen.TSmsInfoState.Enum getType() {
		return type;
	}

	public void setType(com.liz.cptgen.TSmsInfoState.Enum type) {
		this.type = type;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

