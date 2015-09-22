/**
 * ClassName FPGrowthDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;


/**
 * @author Eason
 */
public class FPGrowthDB extends Operator {
    private static final Logger itsLogger = Logger.getLogger(FPGrowthDB.class);

    public ConsumerProducer[] apply() throws OperatorException {
		itsLogger.debug(LogUtils.entry("FPGrowthDB", "apply", ""));
		FPGrowthParameter para = (FPGrowthParameter)getParameter();
		DataSet dataSet = null;
		try {
			dataSet = getInput(DataSet.class);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage(), e);
		}
		AbstractFPGrowthDB fPGrowthDB = null;
		if (para.isUseArray()){
			String dbType = ((DBTable) dataSet.getDBTable()).getDatabaseConnection()
			.getProperties().getName();
			if(dbType.equalsIgnoreCase(DataSourceInfoDB2.dBType)
					||dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.NOT_SUPPORT_ARRAY_ASSOCIATION,AlpineThreadLocal.getLocale()));
			}
			fPGrowthDB = new FPGrowthDBArray();
		}else{
			fPGrowthDB = new FPGrowthDBColumn();
		}
		return fPGrowthDB.apply(dataSet, para);
	}
	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ItemSets.class };
	}
}
