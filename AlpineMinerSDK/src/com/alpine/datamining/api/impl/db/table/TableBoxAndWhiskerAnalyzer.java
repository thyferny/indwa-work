/**
 * ClassName TableAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableBoxAndWhiskerConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutBoxWhisker;
import com.alpine.datamining.api.impl.output.BoxAndWhiskerItem;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;
public class TableBoxAndWhiskerAnalyzer extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(TableBoxAndWhiskerAnalyzer.class);
	

	private ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
	 
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection=null;
		Map<String,BoxAndWhiskerItem> htItem = new LinkedHashMap<String, BoxAndWhiskerItem>();
 
		try {
			TableBoxAndWhiskerConfig config = (TableBoxAndWhiskerConfig) source.getAnalyticConfig();
		  connection = ((DataBaseAnalyticSource)source).getConnection();
		    
		  sqlGenerator=SqlGeneratorMultiDBFactory.createConnectionInfo(source.getDataSourceType());
		 		 
			String tableName = ((DataBaseAnalyticSource)source).getTableInfo().getTableName();
			String shcemaName= ((DataBaseAnalyticSource)source).getTableInfo().getSchema();
			
			tableName=StringHandler.doubleQ(tableName);
			shcemaName=StringHandler.doubleQ(shcemaName);
			 
			if(shcemaName!=null&&shcemaName.trim().length()>0){
				tableName=shcemaName+"."+tableName;
			}
			String valueDomain = config.getAnalysisValueDomain();
			String seriesDomain = config.getSeriesDomain();
			String typeDomain = config.getTypeDomain();
			
			StringBuffer where = new StringBuffer(); 
			where.append(" where ").append(StringHandler.doubleQ(valueDomain)).append(" is not null ");
			StringBuffer sql1Group = new StringBuffer(); 
			StringBuffer whereColumns = new StringBuffer(); 
			StringBuffer sql1Select = new StringBuffer(); 
			StringBuffer otherSqlColumns=new StringBuffer(); 
			if(typeDomain != null && !typeDomain.trim().equals("")){
				typeDomain = StringHandler.doubleQ(typeDomain);
				where.append(" and ").append(typeDomain).append(" is not null ");
				sql1Select.append(typeDomain);
				sql1Group.append(typeDomain);
				whereColumns.append("a.").append(typeDomain).append(" = b.").append(typeDomain);
				otherSqlColumns.append("a.").append(typeDomain);
			}
			if(seriesDomain != null && !seriesDomain.trim().equals("")){
				StringBuffer countSQL=new StringBuffer();
				countSQL.append("select ").append("count(distinct ").append(StringHandler.doubleQ(seriesDomain));
				countSQL.append(") from ").append(tableName).append(where);
				
				logger.info(countSQL.toString());
				ps = createPreparedStatement(connection,countSQL.toString(), false);
				rs = ps.executeQuery();
				while(rs.next()){
					long count = rs.getLong(1);
					if(count>AlpineMinerConfig.BOX_WHISKER_THRESHOLD){
						throw new AnalysisError(this, AnalysisErrorName.VALUE_BOX_EXCEED,config.getLocale(),
								AlpineMinerConfig.BOX_WHISKER_THRESHOLD);//
					}
				}	
				
				seriesDomain = StringHandler.doubleQ(seriesDomain);
				where.append(" and ").append(seriesDomain).append(" is not null ");
				if(sql1Group.toString().trim().equals("")){
					sql1Group.setLength(0);
					sql1Group.append(seriesDomain);
				}else{
					sql1Group.append(",").append(seriesDomain);
				}
				if(whereColumns.toString().trim().equals("")){
					whereColumns.setLength(0);
					whereColumns.append("a.").append(seriesDomain).append(" = b.").append(seriesDomain);
				}else{
					whereColumns.append(" and a.").append(seriesDomain).append(" = b.").append(seriesDomain);
				}
				if(sql1Select.toString().trim().equals("")){
					sql1Select.setLength(0);
					sql1Select.append("'',").append(seriesDomain);
				}else{
					sql1Select.append(",").append(seriesDomain);
				}
				if(otherSqlColumns.toString().trim().equals("")){
					otherSqlColumns.setLength(0);
					otherSqlColumns.append("'',a.").append(seriesDomain);
				}else{
					otherSqlColumns.append(",a.").append(seriesDomain);
				}
			}else{
				if(sql1Select.toString().trim().equals("")){
					sql1Select.setLength(0);
					sql1Select.append("'',''");
				}else{
					sql1Select.append(",''");
				}
				if(otherSqlColumns.toString().trim().equals("")){
					otherSqlColumns.setLength(0);
					otherSqlColumns.append("'',''");
				}else{
					otherSqlColumns.append(",''");
				}
			}
			otherSqlColumns.append(",a.MEDIA");
			valueDomain = StringHandler.doubleQ(valueDomain);		
			
			StringBuffer groupBy = new StringBuffer();
			groupBy.append(" group by ").append(sql1Group).append(" ");
			if(sql1Group == null || sql1Group.toString().trim().equals("")){
				groupBy.setLength(0);
			}
			
			StringBuffer sql1 = new StringBuffer();
			sql1.append("select ").append(sql1Select).append(",min(");
			sql1.append(valueDomain).append("),max(").append(valueDomain);
			sql1.append("),avg(").append(sqlGenerator.castToDouble(valueDomain)).append("),count(");
			sql1.append(valueDomain).append(") from ").append(tableName).append(where).append(groupBy);
			
			if(!StringUtil.isEmpty(typeDomain)){
				sql1.append(" order by ").append(typeDomain);
			}
			
			String claseGroup = sql1Group.toString();
			if(claseGroup == null || claseGroup.trim().equals("")){
				claseGroup = "";
			}else{
				claseGroup = claseGroup+",";
			}
			StringBuffer claseGroupBy = new StringBuffer(" GROUP BY ");
			if(sql1Group== null || sql1Group.toString().trim().equals("")){
				claseGroupBy.setLength(0);
			}else{
				claseGroupBy.append(sql1Group);
			}
			
			StringBuffer partionBy = new StringBuffer(" PARTITION BY  ");
			if(sql1Group == null || sql1Group.toString().trim().equals("")){
				partionBy .setLength(0);
			}else{
				partionBy.append(sql1Group);
			}
			
			if(whereColumns == null || whereColumns.toString().trim().equals("")){
				whereColumns.setLength(0);
				whereColumns.append(" 1=1 ");
			}
			
			//use floor() instead trunc().Testeds in all database.
			StringBuffer mediaSQL = new StringBuffer("SELECT ");
			mediaSQL.append(otherSqlColumns).append(" FROM ( SELECT ").append(claseGroup).append(valueDomain);
			mediaSQL.append(" AS MEDIA ,ROW_NUMBER FROM (SELECT ").append(claseGroup).append(valueDomain).append(", ROW_NUMBER() over (");
			mediaSQL.append(partionBy).append(" ORDER BY ").append(valueDomain).append(") AS ROW_NUMBER FROM ").append(tableName).append(where);
			mediaSQL.append(")  foo)  a JOIN (SELECT ").append(claseGroup).append("floor(count(").append(valueDomain);
			mediaSQL.append(")/2+1) as mediaCount FROM ").append(tableName).append(where).append(claseGroupBy).append(")  b ON (");
			mediaSQL.append(whereColumns).append(" AND a.ROW_NUMBER = b.mediaCount)");
			
			StringBuffer media1SQL = new StringBuffer("SELECT ");
			media1SQL.append(otherSqlColumns).append(" FROM ( SELECT ").append(claseGroup).append(valueDomain);
			media1SQL.append(" AS MEDIA ,ROW_NUMBER FROM (SELECT ").append(claseGroup).append(valueDomain).append(", ROW_NUMBER() over (");
			media1SQL.append(partionBy).append(" ORDER BY ").append(valueDomain).append(") AS ROW_NUMBER FROM ").append(tableName).append(where);
			media1SQL.append(")  foo)  a JOIN (SELECT ").append(claseGroup).append("floor(case when count(").append(valueDomain);
			media1SQL.append(") =1 then 1 else count(").append(valueDomain);
			media1SQL.append(")/2 end ) as mediaCount FROM ").append(tableName).append(where).append(claseGroupBy).append(")  b ON (");
			media1SQL.append(whereColumns).append(" AND a.ROW_NUMBER = b.mediaCount)");
			
			StringBuffer q1SQL = new StringBuffer("SELECT ");
			q1SQL.append(otherSqlColumns).append(" FROM ( SELECT ").append(claseGroup).append(valueDomain);
			q1SQL.append(" AS MEDIA ,ROW_NUMBER FROM (SELECT ").append(claseGroup).append(valueDomain).append(", ROW_NUMBER() over (");
			q1SQL.append(partionBy).append(" ORDER BY ").append(valueDomain).append(") AS ROW_NUMBER FROM ").append(tableName).append(where);
			q1SQL.append(")  foo)  a JOIN (SELECT ").append(claseGroup).append("floor(count(").append(valueDomain);
			q1SQL.append(")/4+1) as q1 FROM ").append(tableName).append(where).append(claseGroupBy).append(")  b ON (");
			q1SQL.append(whereColumns).append(" AND a.ROW_NUMBER = b.q1)");
			
			StringBuffer q3SQL = new StringBuffer("SELECT ");
			q3SQL.append(otherSqlColumns).append(" FROM ( SELECT ").append(claseGroup).append(valueDomain);
			q3SQL.append(" AS MEDIA ,ROW_NUMBER FROM (SELECT ").append(claseGroup).append(valueDomain).append(", ROW_NUMBER() over (");
			q3SQL.append(partionBy).append(" ORDER BY ").append(valueDomain).append(") AS ROW_NUMBER FROM ").append(tableName).append(where);
			q3SQL.append(")  foo)  a JOIN (SELECT ").append(claseGroup).append("floor(count(").append(valueDomain);
			q3SQL.append(")*3/4+1) as q3 FROM ").append(tableName).append(where).append(claseGroupBy).append(")  b ON (");
			q3SQL.append(whereColumns).append(" AND a.ROW_NUMBER = b.q3)");
			
	
			logger.info(sql1.toString());
			ps = createPreparedStatement(connection,sql1.toString(), false);
			
			rs = ps.executeQuery();
			while(rs.next()){
				BoxAndWhiskerItem item = new BoxAndWhiskerItem();
				String value = rs.getString(1);
				item.setType((value == null)? "":value);
				value = rs.getString(2);
				item.setSeries((value == null)? "":value);
				item.setMin(Double.valueOf(rs.getString(3)));
				item.setMax(Double.valueOf(rs.getString(4)));
				item.setMean(Double.valueOf(rs.getString(5)));
				item.setVariableName(valueDomain);
				item.setSeriesName(seriesDomain);
				item.setTypeName(typeDomain);
				htItem.put(item.getType()+"/"+item.getSeries(),item);
			}
			rs.close();
			ps.close();
			
			logger.info(mediaSQL.toString());
			ps = createPreparedStatement(connection,mediaSQL.toString(), false);
			
			rs = ps.executeQuery();
			while(rs.next()){
				BoxAndWhiskerItem item = new BoxAndWhiskerItem();
				String value = rs.getString(1);
				item.setType((value == null)? "":value);
				value = rs.getString(2);
				item.setSeries((value == null)? "":value);
				htItem.get(item.getType()+"/"+item.getSeries()).setMedian(Double.valueOf(rs.getString(3)));
			}
			rs.close();
			ps.close();
			
			logger.info(media1SQL.toString());
			ps = createPreparedStatement(connection,media1SQL.toString(), false);
			
			rs = ps.executeQuery();
			while(rs.next()){
				BoxAndWhiskerItem item = new BoxAndWhiskerItem();
				String value = rs.getString(1);
				item.setType((value == null)? "":value);
				value = rs.getString(2);
				item.setSeries((value == null)? "":value);
				Number median = htItem.get(item.getType()+"/"+item.getSeries()).getMedian();
				Number median1 = Double.valueOf(rs.getString(3));
				htItem.get(item.getType()+"/"+item.getSeries()).setMedian((median.doubleValue()+median1.doubleValue())/2.0);
			}
			rs.close();
			
			ps = createPreparedStatement(connection,q1SQL.toString(), false);
			logger.info(q1SQL.toString());
			rs = ps.executeQuery();
			while(rs.next()){
				BoxAndWhiskerItem item = new BoxAndWhiskerItem();
				String value = rs.getString(1);
				item.setType((value == null)? "":value);
				value = rs.getString(2);
				item.setSeries((value == null)? "":value);
				htItem.get(item.getType()+"/"+item.getSeries()).setQ1(Double.valueOf(rs.getString(3)));
			}
			rs.close();
			ps.close();
			
			
			ps = createPreparedStatement(connection,q3SQL.toString(), false);
			logger.info(q3SQL.toString());
			rs = ps.executeQuery();
			while(rs.next()){
				BoxAndWhiskerItem item = new BoxAndWhiskerItem();
				String value = rs.getString(1);
				item.setType((value == null)? "":value);
				value = rs.getString(2);
				item.setSeries((value == null)? "":value);
				htItem.get(item.getType()+"/"+item.getSeries()).setQ3(Double.valueOf(rs.getString(3)));
			}
			
			AnalyzerOutPutBoxWhisker output = new AnalyzerOutPutBoxWhisker();
            output.setAnalyticNodeMetaInfo(createNodeMetaInfo(source));
            output.setApprox(false);

            Iterator<Entry<String, BoxAndWhiskerItem>> iter = htItem.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, BoxAndWhiskerItem> entry = iter.next();
				output.addItem(entry.getValue());
			}
			return output;
		} catch (Exception e) {
			logger.error(e);
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}finally{
			try {
				if(rs != null)rs.close();
				if(ps != null)ps.close();
				
 
			} catch (SQLException e) {
				logger.error(e );
				throw new AnalysisException(e );
			}
		}
	}



    private AnalyticNodeMetaInfo createNodeMetaInfo(AnalyticSource source) {
        Locale locale= source.getAnalyticConfig().getLocale();
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.BOX_PLOT_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.BOX_PLOT_DESCRIPTION, locale));

        return nodeMetaInfo;
    }
}
