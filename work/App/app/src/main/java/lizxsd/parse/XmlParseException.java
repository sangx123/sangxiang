/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2009-7-31.
 * Create Description
 *
 *  $Id: XmlParseException.java,v 1.1 2011/06/10 09:07:40 zhm Exp $
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

/**
 * 
 */
@SuppressWarnings("serial")
public class XmlParseException extends Exception {
	public XmlParseException(String expected, String actual) {
		super(new StringBuffer("Expectant xml item name: ").append(expected)
				.append(", but actual name is: ").append(actual).toString());
	}
}
