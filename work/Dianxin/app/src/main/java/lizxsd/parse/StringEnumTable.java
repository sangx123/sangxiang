/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2009-7-31.
 * Create Description
 *
 *  $Id: StringEnumTable.java,v 1.1 2011/06/10 09:07:40 zhm Exp $
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

import java.util.Hashtable;


/**
 * 
 */
public class StringEnumTable{
	private static final long serialVersionUID = 1L;

	private String _string;
	private int _int;

	/**
	 * Singleton instances should only be created by subclasses.
	 */
	protected StringEnumTable(String s, int i) {
		_string = s;
		_int = i;
	}

	/** Returns the underlying string value */
	@Override
	public final String toString() {
		return _string;
	}

	public final int intValue() {
		return _int;
	}

	/** Returns the hash code of the underlying string */
	@Override
	public final int hashCode() {
		return _string.hashCode();
	}

	/**
	 * Used to manage singleton instances of enumerations.
	 * Each subclass of StringEnumAbstractBase has an instance
	 * of a table to hold the singleton instances.
	 */
	public static final class Table {
		private Hashtable<String, StringEnumTable> _map;

		public Table(StringEnumTable[] array) {
			_map = new Hashtable<String, StringEnumTable>(array.length);
			for (int i = 0; i < array.length; i++) {
				_map.put(array[i].toString(), array[i]);
			}
		}

		/** Returns the singleton for a {@link String}, or null if none. */
		public StringEnumTable forString(String s) {
			return _map.get(s);
		}
	}
}
