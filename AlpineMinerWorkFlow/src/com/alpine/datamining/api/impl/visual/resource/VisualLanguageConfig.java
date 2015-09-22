/**
 * ClassName VisualLanguageConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-5-28
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual.resource;

import java.util.Locale;
import java.util.ResourceBundle;

public class VisualLanguageConfig {

	static Locale locale = Locale.getDefault();
	public static ResourceBundle rb = ResourceBundle.getBundle(
			"com.alpine.datamining.api.impl.visual.resource.visualConfig",locale);
	
	public static final String NB_TABLE_COLUMN_LIMIT=rb.getString("NB_TABLE_COLUMN_LIMIT");
	public static final String SPLITMODEL_GROUP_LIMIT=rb.getString("SPLITMODEL_GROUP_LIMIT");
}
