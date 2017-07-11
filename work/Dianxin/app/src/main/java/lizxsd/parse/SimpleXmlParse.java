/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2009-7-31.
 * Create Description
 *
 *  $Id: SimpleXmlParse.java,v 1.2 2011/06/12 06:36:31 zhm Exp $
 *
 ************************* To  Do ***********************************
 *
 ************************* Others ***********************************
 * Add anything you want to write here.
 * 
 * 
 ******************************************************************
 */
package lizxsd.parse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * SimpleXmlParse doesn't check the xml syntax, Suppose that everything is validated.
 * 
 * Next time should takeName or takeValue the xsd protocol should do this work.
 * Ω‚Œˆ¿‡
 */
public class SimpleXmlParse {
	int cursor;
	final String text;
	final int length;
	
	boolean nextValueEmpty;
	
	boolean takePreviousName = false;
	String previousName = null;
	
	public SimpleXmlParse(String text) {
		this.text = text;
		length = text.length();
		nextValueEmpty = false;
		cursor = 0;
		
		//take the encode, now I just parse the UTF-8 encode.
		takeEncode();
	}
	
	public final static char XML_START = '<';
	public final static char XML_END = '>';
	public final static char XML_CLOSET = '/';
	public final static char XML_COMMENT = '!';
	
	//first line of xml;
	public final static char XML_ENCODE = '?';
	
	/**
	 * append the xml start name
	 * @param buffer
	 * @param name
	 */
	static public void appendXmlStart(StringBuffer buffer, String name) {
		buffer.append(XML_START);
		buffer.append(name);
		buffer.append(XML_END);
	}
	
	/**
	 * append the xml end.
	 * @param buffer
	 * @param name
	 */
	static public void appendXmlEnd(StringBuffer buffer, String name) {
		buffer.append(XML_START);
		buffer.append(XML_CLOSET);
		buffer.append(name);
		buffer.append(XML_END);
	}
	
	/**
	 * append the int.
	 * @param buffer
	 * @param name
	 * @param v
	 */
	static public void appendInt(StringBuffer buffer, String name, int v) {
		appendXmlStart(buffer, name);
		buffer.append(v);
		appendXmlEnd(buffer, name);
	}
	
	/**
	 * append integer
	 * @param buffer
	 * @param name
	 * @param v
	 */
	static public void appendInteger(StringBuffer buffer, String name, Integer v) {
		appendXmlStart(buffer, name);
		buffer.append(v);
		appendXmlEnd(buffer, name);
	}
	
	/**
	 * append the string.
	 * @param buffer
	 * @param name
	 * @param v
	 */
	static public void appendString(StringBuffer buffer, String name, String v) {
		appendXmlStart(buffer, name);
		buffer.append(v);
		appendXmlEnd(buffer, name);
	}

	/**
	 * Zone convert: +08:00 -> +0800
	 * 
	 * @param v
	 * @return
	 */
	public static String convertTimeZene(String v) {
		if (v.length() < 3) {
			return "";
		}

		return v.substring(0, v.length() - 3) + v.substring(v.length() - 2);
	}

	final static private String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	final static private String TIME_FORMAT_PATTERN = "HH:mm:ss.SSSZ";
	final static private String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static java.util.Calendar parseTime(String value) throws ParseException {
		
		value = convertTimeZene(value);
		SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
		Date date = format.parse(value);

		Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

	public static java.util.Calendar parseDate(String value) throws ParseException {

		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		Date date = format.parse(value);

		Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

	public static java.util.Calendar parseDateTime(String value) throws ParseException {

		value = convertTimeZene(value);

		SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);
		Date date = format.parse(value);

		Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}
	
	/**
	 * encode time
	 * 
	 * @param time
	 * @return
	 */
	public static void encodeTime(Calendar time, StringBuffer buffer) {
		SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
		buffer.append(format.format(time.getTime()));
		
		//The format is "12:08:56.235-0700", need change to "12:08:56.235-07:00"
		buffer.insert(buffer.length() - 2, ':');
	}

	/**
	 * encode date
	 * 
	 * @param date
	 * @return
	 */
	public static void encodeDate(Calendar date, StringBuffer buffer) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		buffer.append(format.format(date.getTime()));
	}

	/**
	 * encoder the date time.
	 * 
	 * @param dateTime
	 * @return
	 */
	public static void encodeDateTime(Calendar dateTime, StringBuffer buffer) {
		SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);

		// DontCareFieldPosition
		buffer.append(format.format(dateTime.getTime()));

		buffer.insert(buffer.length() - 2, ':');
	}
	
	/**
	 * 	take the encode like:<?xml version="1.0" encoding="UTF-8"?>
	 * @return
	 */
	public void takeEncode() {
		
		//
		boolean findStart = false;
		boolean encodeText = false;
		for (int i = cursor; i < length; i++) {
			char c = text.charAt(i);
			
			if (findStart) {
				//find next encode text;
				if (encodeText) {
					if (c == XML_ENCODE) {
	 					//find the '?' then think take the end, then set cursor
						cursor = i + 2;
						return;
					} else {
						continue;
					}
				}
				else {
					if (c == XML_ENCODE) {
						encodeText = true;
					} else {
						//doing nothing, because doesn't start with "<?"
						return;
					}
				}
			} else {
				if (c == XML_START) {
					findStart = true;
				}
			}
		}
	}
	
	/**
	 * And after doing this the cursor moved to the next of xml name.
	 * eg: <seq_id>112233</seq_id> move to first '1';
	 * 
	 * @return Return the xml name, eg: <seq_id>112233</seq_id>; return the: seq_id,
	 * 		<seq_id/>: take the: seq_id, and set next value to empty.
	 */
	public String takeName() {
		
		if (takePreviousName) {
			takePreviousName = false;
			return previousName;
		}
		
		boolean findStart = false;
		for (int i = cursor; i < length; i++) {
			
			char c = text.charAt(i);
			if (findStart) {
				if (c == XML_END) {
					//deal for case: <seq_id/>
					
					if (text.charAt(i - 1) == XML_CLOSET) {
						nextValueEmpty = true;
						previousName = text.substring(cursor, i - 1);
					} else {
						previousName = text.substring(cursor, i);
					}
					cursor = i + 1;
					return previousName;
				}
			} else {
				if (c == XML_START) {
					findStart = true;
					cursor = i + 1;
					nextValueEmpty = false;
				}
			}
		}
		
		//find nothing.
		cursor = length;
		return null;
	}
	
	/**
	 * must call takeName(), then call take takeValue().
	 * 
	 * Enter this function ,the cursor in the first character of value, after doing this the cursor will move to end of XML name.
	 * 
	 * eg: <seq_id>112233</seq_id> move to the next of end '>'.
	 * 
	 * @return
	 */
	public String takeValue() {
		
		if (nextValueEmpty) {
			return "";
		}
		
		String value = null;
		boolean findStart = false;
		for (int i = cursor; i < length; i++) {
			char c = text.charAt(i);
			
			if (findStart) {
				if (c == XML_END) {
					cursor = i + 1;
					return value;
				}
				//continue here
			} else {
				if (c == XML_START) {
					value = text.substring(cursor, i);
					findStart = true;
				}
			}
		}
		
		return null;
	}

	/** @param takePreviousName The takePreviousName to set.
	 */
	public void takePreviousName() {
		this.takePreviousName = true;
	}
}
