package com.liz.cptr;

import lizxsd.parse.IPacket;
import lizxsd.parse.SimpleXmlParse;
import lizxsd.parse.XmlParseException;

public class TPhonebookReturn implements IPacket { 

	Integer pbId = null;
	String phoneNumber;
	String userName = null;
	String comment = null;
	com.liz.cptr.TBlackwhiteState.Enum state;

	final private static String PB_ID = "pb_id";
	final private static String PHONE_NUMBER = "phone_number";
	final private static String USER_NAME = "user_name";
	final private static String COMMENT = "comment";
	final private static String STATE = "state";


	private TPhonebookReturn() {

	}

	public static class Factory {
		static public TPhonebookReturn newInstance() {
			return new TPhonebookReturn();
		}
	}


	static public TPhonebookReturn parse(SimpleXmlParse parse) throws XmlParseException, Exception{
		
		TPhonebookReturn packet = new TPhonebookReturn();
		String name;
		
		name = parse.takeName();
		if (name.equals(PB_ID)) {
			packet.pbId = Integer.valueOf(parse.takeValue());
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(PHONE_NUMBER)) {
			throw new XmlParseException(PHONE_NUMBER, name);
		}
		packet.phoneNumber = parse.takeValue();

		name = parse.takeName();
		if (name.equals(USER_NAME)) {
			packet.userName = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (name.equals(COMMENT)) {
			packet.comment = parse.takeValue();
		} else {
			parse.takePreviousName();
		}

		name = parse.takeName();
		if (!name.equals(STATE)) {
			throw new XmlParseException(STATE, name);
		}
		packet.state = com.liz.cptr.TBlackwhiteState.Enum.parse(parse.takeValue());

		
		parse.takeName();
		return packet;
	}
	/**
	 * @see com.sac.utility.IPacket#encode(java.lang.StringBuffer)
	 */
	public void encode(StringBuffer buffer) {
		
		if (pbId != null) {
			SimpleXmlParse.appendXmlStart(buffer, PB_ID);
			buffer.append(pbId);
			SimpleXmlParse.appendXmlEnd(buffer, PB_ID);
		}

		SimpleXmlParse.appendXmlStart(buffer, PHONE_NUMBER);
		buffer.append(phoneNumber);
		SimpleXmlParse.appendXmlEnd(buffer, PHONE_NUMBER);

		if (userName != null) {
			SimpleXmlParse.appendXmlStart(buffer, USER_NAME);
			buffer.append(userName);
			SimpleXmlParse.appendXmlEnd(buffer, USER_NAME);
		}

		if (comment != null) {
			SimpleXmlParse.appendXmlStart(buffer, COMMENT);
			buffer.append(comment);
			SimpleXmlParse.appendXmlEnd(buffer, COMMENT);
		}

		SimpleXmlParse.appendXmlStart(buffer, STATE);
		buffer.append(state);
		SimpleXmlParse.appendXmlEnd(buffer, STATE);

		
	}
	
	public String getXmlTagName() {
		return "t_phonebook_return";
	}
	public Integer getPbId() {
		return pbId;
	}

	public void setPbId(Integer pbId) {
		this.pbId = pbId;
	}

	public boolean isSetPbId() {
		return (this.pbId != null);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isSetUserName() {
		return (this.userName != null);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isSetComment() {
		return (this.comment != null);
	}

	public com.liz.cptr.TBlackwhiteState.Enum getState() {
		return state;
	}

	public void setState(com.liz.cptr.TBlackwhiteState.Enum state) {
		this.state = state;
	}



	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		encode(buffer);
		return buffer.toString();
	}
}

