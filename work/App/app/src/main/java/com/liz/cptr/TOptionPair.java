package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TOptionPair implements IPacket { 

	int id;
	int value;

	final private static String ID = "id";
	final private static String VALUE = "value";


	private TOptionPair() {

	}

	public static class Factory {
		static public TOptionPair newInstance() {
			return new TOptionPair();
		}
	}


	static public TOptionPair parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TOptionPair packet = new TOptionPair();
		String name;
		
		name = parse.takeName();
		if (!name.equals(ID)) {
			throw new XmlParseException(ID, name);
		}
		packet.id = Integer.parseInt(parse.takeValue());

		name = parse.takeName();
		if (!name.equals(VALUE)) {
			throw new XmlParseException(VALUE, name);
		}
		name = parse.takeValue();
//		packet.value = Integer.parseInt(parse.takeValue());
		if (name.toLowerCase().equals("true")) {
			packet.value = 1;
		} else if (name.toLowerCase().equals("false")) {
			packet.value = 0;
		} else {
			packet.value = Integer.parseInt(name);
		}

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		SimpleXmlParse.appendXmlStart(buffer, ID);
		buffer.append(id);
		SimpleXmlParse.appendXmlEnd(buffer, ID);

		SimpleXmlParse.appendXmlStart(buffer, VALUE);
		buffer.append(value);
		SimpleXmlParse.appendXmlEnd(buffer, VALUE);

		
	}
	
	public String getXmlTagName() {
		return "t_option_pair";
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

