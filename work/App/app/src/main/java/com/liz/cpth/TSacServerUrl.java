package com.liz.cpth;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TSacServerUrl implements IPacket { 

	String name;
	String sacUrl;

	final private static String NAME = "name";
	final private static String SAC_URL = "sac_url";


	private TSacServerUrl() {

	}

	public static class Factory {
		static public TSacServerUrl newInstance() {
			return new TSacServerUrl();
		}
	}


	static public TSacServerUrl parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TSacServerUrl packet = new TSacServerUrl();
		String name;
		
		name = parse.takeName();
		if (!name.equals(NAME)) {
			throw new XmlParseException(NAME, name);
		}
		packet.name = parse.takeValue();

		name = parse.takeName();
		if (!name.equals(SAC_URL)) {
			throw new XmlParseException(SAC_URL, name);
		}
		packet.sacUrl = parse.takeValue();

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		SimpleXmlParse.appendXmlStart(buffer, NAME);
		buffer.append(name);
		SimpleXmlParse.appendXmlEnd(buffer, NAME);

		SimpleXmlParse.appendXmlStart(buffer, SAC_URL);
		buffer.append(sacUrl);
		SimpleXmlParse.appendXmlEnd(buffer, SAC_URL);

		
	}
	
	public String getXmlTagName() {
		return "t_sac_server_url";
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSacUrl() {
		return sacUrl;
	}

	public void setSacUrl(String sacUrl) {
		this.sacUrl = sacUrl;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

