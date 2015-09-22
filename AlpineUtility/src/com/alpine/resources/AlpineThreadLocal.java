/**
 * ClassName AlpineThreadLocal.java
 *
 * Version information: 1.00
 *
 * Data: 2011-12-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.resources;

import java.util.Locale;

public class AlpineThreadLocal {
	public static Locale getLocale() {
		return (Locale)Locale_Thread_Local.get();
	}

	public static void setLocale(Locale locale) {
		Locale_Thread_Local.set(locale) ;
	}
	
	public static void clearLocale() {
		Locale_Thread_Local.set(null) ;
	}
	private static final ThreadLocal<Locale>  Locale_Thread_Local = new ThreadLocal<Locale>();  
}
