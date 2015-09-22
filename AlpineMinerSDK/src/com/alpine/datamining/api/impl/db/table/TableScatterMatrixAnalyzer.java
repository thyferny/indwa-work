package com.alpine.datamining.api.impl.db.table;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CorrelationAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.TableScatterMatrixConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.CorrelationAnalysisAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class TableScatterMatrixAnalyzer extends AbstractDBAnalyzer{
	private static Logger logger= Logger.getLogger(TableScatterMatrixAnalyzer.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		TableScatterMatrixConfig config = (TableScatterMatrixConfig) source.getAnalyticConfig();
	 
		String schemaName =((DataBaseAnalyticSource)source).getTableInfo().getSchema(); 
		String tableName= ((DataBaseAnalyticSource)source).getTableInfo().getTableName(); 
		String dbSystem = ((DataBaseAnalyticSource)source).getDataBaseInfo().getSystem(); 
		Connection conn = ((DataBaseAnalyticSource)source).getConnection();
		DBDataUtil dbd = new DBDataUtil(conn, dbSystem);
		dbd.setLocale(config.getLocale());
		
		Map<ScatterMatrixColumnPairs,DataTable> dataTableMap=new LinkedHashMap<ScatterMatrixColumnPairs,DataTable>();
		ArrayList<Double> corrList = new ArrayList<Double>();
		
		//Default value is 200.
		String maxRows= ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT);
			
		try {
			String columnNames = config.getColumnNames();
			String[] columnNamesArray = columnNames.split(",");
			for(int i=0;i<columnNamesArray.length;i++){
				for(int j=i+1;j<columnNamesArray.length;j++){
					DataTable dt = dbd.getSampleTableDataList(schemaName,
							tableName,new String[]{columnNamesArray[i],columnNamesArray[j]},maxRows, 
							columnNamesArray[i]);
					dataTableMap.put(new ScatterMatrixColumnPairs(columnNamesArray[i],columnNamesArray[j]), dt);
				}
			}
			CorrelationAnalysisConfig correlationAnalysisConfig = new CorrelationAnalysisConfig();
			correlationAnalysisConfig.setColumnNames(columnNames);
			source.setAnalyticConfiguration(correlationAnalysisConfig);
			CorrelationAnalysisAnalyzer corrAnalyzer=new CorrelationAnalysisAnalyzer();
			AnalyzerOutPutObject corrOutput=(AnalyzerOutPutObject)corrAnalyzer.doAnalysis(source);
			Object[] obj = (Object[])corrOutput.getOutPutObject();
			HashMap<String, Double> corrResultMap = (HashMap<String,Double>)obj[1];
			for(int i=0;i<columnNamesArray.length;i++){
				for(int j=i+1;j<columnNamesArray.length;j++){
					Double dd = corrResultMap.get(columnNamesArray[i]+"/"+columnNamesArray[j]);
					corrList.add(dd);
				}
			}
			//Must set back to TableScatterMatrixConfig for step run "equals".
			source.setAnalyticConfiguration(config);
			AnalyzerOutPutScatterMatrix output = new AnalyzerOutPutScatterMatrix();
			output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			output.setDataTableMap(dataTableMap);
			output.setCorrList(corrList);
			output.setColumnNames(columnNamesArray);
			return output;
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SCATTERMATRIX_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SCATTERMATRIX_DESCRIPTION,locale)); 
		return nodeMetaInfo;
	}
	
}

