/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2009-7-31.
 * Create Description
 *
 *  $Id: IPacket.java,v 1.1 2011/06/10 09:07:39 zhm Exp $
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
 * 数据类型
 */
public interface IPacket {
	
	//for some CLDC doesn't define TRUE or FALSE.
	public final static Boolean TRUE = new Boolean(true);
	public final static Boolean FALSE = new Boolean(false);
	
	/**
	 * get the packet name.
	 */
	public String getXmlTagName();
	
	/**
	 * encode the packet.
	 * @param buffer
	 */
	public void encode(StringBuffer buffer);
}
