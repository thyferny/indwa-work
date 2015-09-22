package com.alpine.resources;

import java.util.Locale;
import java.util.ResourceBundle;

public class AlpineUtilityConfig {

	static Locale locale = Locale.getDefault();
	public static ResourceBundle rb = ResourceBundle.getBundle("com.alpine.resources.AlpineUtilityConfig",locale);
	public static final long SAMPLE_DISTINCT_COUNT = Long.parseLong(rb.getString("SAMPLE_DISTINCT_COUNT"));
}
