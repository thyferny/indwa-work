/**
 * ClassName Tools
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;


/**
 * CommonUtility 
 * 
 * @author Eason
 */
public class Tools {

	/** The line separator depending on the operating system. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");


	/** Number smaller than this value are considered as zero. */
	private static final double IS_ZERO = 1E-6;

	/** Returns true if the difference between both numbers is smaller than IS_ZERO. */
	public static boolean isEqual(double d1, double d2) {
		return Math.abs(d1 - d2) < IS_ZERO;
	}

	/** Returns {@link #isEqual(double, double)} for d and 0. */
	public static boolean isZero(double d) {
		return isEqual(d, 0.0d);
	}

	/** Returns no {@link #isEqual(double, double)}. */
	public static boolean isNotEqual(double d1, double d2) {
		return !isEqual(d1, d2);
	}

	/** Returns true if the d1 is greater than d2 and they are not equal. */
	public static boolean isGreater(double d1, double d2) {
		return (d1 > d2) && isNotEqual(d1, d2);
	}

	/** Returns true if the d1 is greater than d1 or both are equal. */
	public static boolean isGreaterEqual(double d1, double d2) {
		return (d1 > d2) || isEqual(d1, d2);
	}

	/** Returns true if the d1 is less than d2 and they are not equal. */
	public static boolean isLess(double d1, double d2) {
		return !isGreaterEqual(d1, d2);
	}

	/** Returns true if the d1 is less than d1 or both are equal. */
	public static boolean isLessEqual(double d1, double d2) {
		return !isGreater(d1, d2);
	}

	// ====================================

	/** Returns the correct line separator for the current operating system. */
	public static String getLineSeparator() {
		return LINE_SEPARATOR;
	}

	/** Returns the correct line separator for the current operating system concatenated 
	 *  for the given number of times. */
	public static String getLineSeparators(int number) {
		if (number < 0)
			number = 0;
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < number; i++)
			result.append(LINE_SEPARATOR);
		return result.toString();
	}


	/**
	 * Returns the class name of the given class without the package
	 * information.
	 */
	public static String classNameWOPackage(Class<?> c) {
		return c.getName().substring(c.getName().lastIndexOf(".") + 1);
	}

}
