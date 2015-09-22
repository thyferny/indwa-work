package com.alpine.datamining.api;

import com.alpine.datamining.MinerInit;
import org.apache.log4j.Logger;

public class AlpineMinerSDK {
    private static final Logger itsLogger = Logger.getLogger(AlpineMinerSDK.class);
    public static void init()
	{
		itsLogger.info("AlpineMinerSDK init ...");
		MinerInit.init();
	}
}
