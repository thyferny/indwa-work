/**
 * ClassName MinerInit
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining;

import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;

/**
 * 
 * @author Eason
 */
public class MinerInit {


	private static boolean init = false;
	
	private static boolean useCFunction = true;

    private static final Logger itsLogger = Logger.getLogger(MinerInit.class);


    /**
	 * Initializes Miner.
	 * 
	 */
	private static void initAll() {

		DatabaseUtil.init();

		AlpineMath.setDecimalPrecision(Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_DIGIT_PRECISION)));
		
		//LogService.getInstance().setLogLevel();
		
		if (AlpineDataAnalysisConfig.USE_C_FUNCTION.equalsIgnoreCase("true"))
		{
			useCFunction = true;
		}
		else
		{
			useCFunction = false;
		}
	}


	public static void init()//String driver)
	{
		itsLogger.debug(LogUtils.entry("MinerInit", "init", ""));

		if (init == true)
			return;
		try {
			initAll();
		} catch (Exception e) {
			itsLogger.error("init error:" + e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
		init = true;
		itsLogger.debug(LogUtils.exit("MinerInit", "init", ""));
	}

	public static boolean isUseCFunction() {
		return useCFunction;
	}

	public static void setUseCFunction(boolean useCFunction) {
		MinerInit.useCFunction = useCFunction;
	}
}
