package com.alpine.datamining.api.impl.db.table;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableUnivariateConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutUnivariate;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class TableUnivariateAnalyzer extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(TableUnivariateAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		TableUnivariateConfig config = (TableUnivariateConfig) source.getAnalyticConfig();
 
		String schemaName  =((DataBaseAnalyticSource)source).getTableInfo().getSchema(); 
		String tableName= ((DataBaseAnalyticSource)source).getTableInfo().getTableName(); 
	
		List<String> allSelectedColumns = config.getAllSelectedColumn();
		String referenceColumn = config.getReferenceColumn();
		String dbSystem = ((DataBaseAnalyticSource)source).getDataBaseInfo().getSystem(); 
		Connection conn = ((DataBaseAnalyticSource)source).getConnection();
		DBDataUtil dbd = new DBDataUtil(conn, dbSystem);
		dbd.setLocale(config.getLocale());
		try {
			String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
			
			DataTable dt = dbd.getSampleTableDataList(schemaName, tableName, allSelectedColumns.toArray(
					new String[allSelectedColumns.size()]),maxRows,    referenceColumn);
			AnalyzerOutPutUnivariate output = new AnalyzerOutPutUnivariate();
			output.setDataTable(dt);
			output.setAnalysisColumn(config.getAnalysisColumn());
			output.setReferenceColumn(config.getReferenceColumn());
			output.setAllSelectedColumn(config.getAllSelectedColumn());
			return output;
		} catch (Exception e) {
			logger.error(e );
			throw new AnalysisException(e);
		}
	}

}
