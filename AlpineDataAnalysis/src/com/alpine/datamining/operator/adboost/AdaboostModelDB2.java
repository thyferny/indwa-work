


package com.alpine.datamining.operator.adboost;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;


public class AdaboostModelDB2 extends AdaboostModel{

	
	private static final Logger itsLogger = Logger.getLogger(AdaboostModelDB2.class);
	private static final long serialVersionUID = 7888838451362831185L;
	private String idtable=new String();
	
	Connection DB2Connection = null;

	
	
	public AdaboostModelDB2(DataSet paramDataSet) {
		super(paramDataSet);
		}

	
	@Override
	protected StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator) {
  					while (localIterator.hasNext()) {
						String str = (String) localIterator.next();
						str = StringHandler.doubleQ(str);
						str = str.substring(1, str.length() - 1);
						inforArray.append( str + ",");
					}
					inforArray.deleteCharAt(inforArray.length() - 1);
//					inforArray.append(")");
				return inforArray;
	}

	
	@Override
	protected String adaboostPredictionInit(String outTable,
			String tempOutTable, long timeStamp, String schemaName,
			String dependentColumn, Statement st, StringBuffer inforArray)
			throws OperatorException {
		try {
			idtable="id"+timeStamp;
			String[] columnsArray=inforArray.toString().split(",");
			Array sqlArray = DB2Connection.createArrayOf(
					"VARCHAR", columnsArray);
			String schema=null;
			 if(outTable.contains(".")) {
				 String[] schemaTable=outTable.split("\\.",2);
				 schema=schemaTable[0];
				 outTable=schemaTable[1];
			}
			 
				StringBuffer sql = new StringBuffer();
				sql.append("call alpine_miner_adaboost_initpre(");
				sql.append(StringUtil.isEmpty(schemaName) ? "null" : ("'"+StringHandler.doubleQ(schemaName)+"'"));
				sql.append(",'");
				sql.append(outTable);
				sql.append("','");
				sql.append(timeStamp);
				sql.append("','");
				sql.append(StringHandler.doubleQ(dependentColumn));
				sql.append("',?)");
				
				
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelDB2.adaboostPredictionInit():sql="+sql.toString());
				}
				CallableStatement stpCall = DB2Connection
				.prepareCall(sql.toString());

				stpCall.setArray(1, sqlArray);
				stpCall.execute();
				stpCall.close();
				
				
						
				
				
				String sql1 = "CREATE TABLE " + (StringUtil.isEmpty(schemaName) ? "" : (StringHandler.doubleQ(schemaName)+".")) + StringHandler.doubleQ(tempOutTable)
						+ "  as (select * from  " + (StringUtil.isEmpty(schema) ? "" : (StringHandler.doubleQ(schema)+"."))+outTable +" ) DEFINITION ONLY ";
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelDB2.adaboostPredictionInit():sql="+sql1);
				}
				st.execute(sql1);
				
				sql1=" insert into "+ (StringUtil.isEmpty(schemaName) ? "" : (StringHandler.doubleQ(schemaName)+".")) + StringHandler.doubleQ(tempOutTable)
				+ " select * from  " + (StringUtil.isEmpty(schema) ? "" : (StringHandler.doubleQ(schema)+"."))+outTable;
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelDB2.adaboostPredictionInit():sql="+sql1);
				}
				st.execute(sql1);
				tempOutTable = (StringUtil.isEmpty(schemaName) ? "" : (StringHandler.doubleQ(schemaName) + "."))+StringHandler.doubleQ(tempOutTable);
				sql1="CALL SYSPROC.ADMIN_CMD(' REORG TABLE " + tempOutTable+" ')";
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelDB2.adaboostPredictionInit():sql="+sql1);
				}
				st.execute(sql1);
			
		} catch (SQLException e1) {
			throw new OperatorException(e1.getLocalizedMessage());
		}
		return tempOutTable;
	}

	
	@Override
	protected void adaboostPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, double algWeight,
			Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException {
		
//			this.DB2Connection;
//					sampleArray.append("varchar2array(");

					while (sampleDvalueIterator.hasNext()) {
						String str = (String) sampleDvalueIterator.next();
						str = StringHandler.doubleQ(str);
						str = str.substring(1, str.length() - 1);
						sampleArray.append(str);
						sampleArray.append(",");
					}
					sampleArray
							.deleteCharAt(sampleArray.length() - 1);
//					sampleArray.append(")");
					String[] sampleValues=sampleArray.toString().split(","); 
					Array sqlArray = this.DB2Connection.createArrayOf(
							"VARCHAR", sampleValues);
					StringBuffer sql = new StringBuffer();
					sql.append("call alpine_miner_adaboost_prestep('");
					sql.append(outTable);
					sql.append("','");
					sql.append(StringHandler.doubleQ(tempOutTable));
					sql.append("','");
					sql.append(dependentColumn);
					sql.append("',");
					sql.append(algWeight);
					sql.append(",?)");
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionPredictStep():sql="+sql.toString());
					}
					CallableStatement stpCall = this.DB2Connection
					.prepareCall(sql.toString());
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionPredictStep():sql="+sql.toString());
					}
					stpCall.setArray(1, sqlArray);
					
					
					stpCall.execute();
					
				
					stpCall.close();
					
					
				
		
	}

	
	@Override
	protected void adaboostPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray, double sumc,DataSet dataSet) throws OperatorException {
		
			try {
				
					StringBuffer sql = new StringBuffer();
					sql.append("call alpine_miner_adaboost_prere('");
					sql.append(outTable);
					sql.append("','");
					sql.append(dependentColumn.replace("\"", "\"\""));
					sql.append("',");
					
					sql.append((getLabel().isNumerical() ? 1 : 0));
					sql.append(",?) ");
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+sql.toString());
					}
					CallableStatement stpCall = this.DB2Connection
					.prepareCall(sql.toString());
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+sql.toString());
					}
					String[] infoValues=inforArray.toString().split(","); 
					Array sqlArray = this.DB2Connection.createArrayOf(
							"VARCHAR", infoValues);
					stpCall.setArray(1, sqlArray);
					
					
					stpCall.execute();
					
				
					stpCall.close();
					
					String sql1=null;
					String schema=null;
					 if(tempOutTable.contains(".")) {
						 String[] schemaTable=tempOutTable.split("\\.");
						 schema=schemaTable[0];
						 tempOutTable=schemaTable[1];
					
					sql1 = "call PROC_DROPSCHTABLEIFEXISTS( '" + schema+"','"+tempOutTable+"')";
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+sql1);
					}
					st.execute(sql1);
					 }
					String sql2 = "alter table " + outTable
					+ " drop column \"alpine_adaboost_id\"";
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug(sql2);
					}
					st.execute(sql2);
					sql1="CALL SYSPROC.ADMIN_CMD(' REORG TABLE " + outTable+" ')";
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+sql1);
					}
					st.execute(sql1);
					 sql1 = "call PROC_DROPSCHTABLEIFEXISTS( '" + schema+"','"+StringHandler.doubleQ(idtable)+"')";
					 if(itsLogger.isDebugEnabled()){
							itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+sql1);
					 }
					 st.execute(sql1);
			StringBuffer normaString = new StringBuffer();
			normaString.append("update " + outTable + " set ");
			Iterator<String> localIterator = getTrainingHeader().getColumns()
			.getLabel().getMapping().getValues().iterator();
			while (localIterator.hasNext()) {
				String str = (String) localIterator.next();
				str=StringHandler.doubleQ(str);
				str=str.substring(1, str.length()-1);
				normaString.append("\"").append(Column.CONFIDENCE_NAME).append("(").append(str).append(")\"=")
				.append("\"").append(Column.CONFIDENCE_NAME).append("(").append(str).append( ")\"/").append(sumc).append(" ,");
			}
			normaString.deleteCharAt(normaString.length() - 1);
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelDB2.adaboostPredictionResult():sql="+normaString.toString());
			}
			st.execute(normaString.toString());
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
}
