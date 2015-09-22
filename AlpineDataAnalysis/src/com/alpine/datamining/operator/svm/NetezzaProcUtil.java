/**
 * ClassName NetezzaProcUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2011-12-28
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.utility.tools.StringHandler;

/**
 * 
 * @author Eason
 *
 */
public class NetezzaProcUtil implements Serializable {
	private static final long serialVersionUID = -4806059428506736319L;
	private String whereCondTableName;
	private String columnTableName;
	private String weightsTableName;
	private String individualsTableName;
	private String modelStatsTableName;
	String[] getWhereCondArrayNZ(DataSet newDataSet){
		String[] ret = new String[0];
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			columnsArray.add(columnName);
		}
		return columnsArray.toArray(ret);
	}
	public String[] getColumnsArrayNZ(DataSet newDataSet){
		String[] ret = new String[0];
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			columnsArray.add(columnName);
		}
		return columnsArray.toArray(ret);
	}

	public void initTable(Statement st, boolean predict) throws OperatorException{
		long currentTime = System.currentTimeMillis();
		whereCondTableName = "WC" + currentTime;
		columnTableName = "C" + currentTime;
		weightsTableName = "W" + currentTime;
		individualsTableName = "I" + currentTime;
		if(!predict){
			modelStatsTableName = "M" + currentTime;
		}
		TableTransferParameter.createStringTable(whereCondTableName,st);
		TableTransferParameter.createStringTable(columnTableName,st);
		TableTransferParameter.createDoubleTable(weightsTableName,st);
		TableTransferParameter.createDoubleTable(individualsTableName,st);
		if(!predict){
			TableTransferParameter.createDoubleTable(modelStatsTableName,st);
		}
	}
	public void dropProcTable(Statement st, boolean predict) throws OperatorException {
		TableTransferParameter.dropResultTable(whereCondTableName,st);
		TableTransferParameter.dropResultTable(columnTableName,st);
		TableTransferParameter.dropResultTable(weightsTableName,st);
		TableTransferParameter.dropResultTable(individualsTableName,st);
		if(!predict){
			TableTransferParameter.dropResultTable(modelStatsTableName,st);
		}
	}
	public String getWhereCondTableName() {
		return whereCondTableName;
	}
	public void setWhereCondTableName(String whereCondTableName) {
		this.whereCondTableName = whereCondTableName;
	}
	public String getColumnTableName() {
		return columnTableName;
	}
	public void setColumnTableName(String columnTableName) {
		this.columnTableName = columnTableName;
	}
	public String getWeightsTableName() {
		return weightsTableName;
	}
	public void setWeightsTableName(String weightsTableName) {
		this.weightsTableName = weightsTableName;
	}
	public String getIndividualsTableName() {
		return individualsTableName;
	}
	public void setIndividualsTableName(String individualsTableName) {
		this.individualsTableName = individualsTableName;
	}
	public String getModelStatsTableName() {
		return modelStatsTableName;
	}
	public void setModelStatsTableName(String modelStatsTableName) {
		this.modelStatsTableName = modelStatsTableName;
	}
	
	public String [] generatePredictColumnsSqlArray(DataSet dataSet, HashMap<String,HashMap<String,String>> allTransformMap_valueKey){
		String [] columns = new String[0];
		Columns atts=dataSet.getColumns();
		Iterator<Column> atts_i=atts.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{
				columnsArray.add(columnName);
			}else
			{
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=allTransformMap_valueKey.get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					if (TransformMap_valueKey.containsKey(value)){
	 	 					value=StringHandler.escQ(value);
 	 						String caseStr = "(case  when "+columnName+"='"+value+"' then 1  else 0 end)";
 	 						caseStr = StringHandler.escQ(caseStr);
	 	 					columnsArray.add(caseStr);
 	 					}
 	 				}
			}
		}
		columns = columnsArray.toArray(columns);
		return columns;
	}
	
	public String[] generatePredictColumnWhere(DataSet dataSet){
		String [] whereArray = new String[dataSet.getColumns().size()];
		Columns attsNew = dataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		int i = 0;
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			whereArray[i] = columnName;
			i++;
		}
		return whereArray;
	}
}
