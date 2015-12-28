
package com.alpine.datamining.operator.svm;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.tools.StringHandler;



public abstract class SVMModel extends Prediction {

	private static final long serialVersionUID = 4486928292072567374L;
	private DataSet newDataSet;
	private HashMap<String,HashMap<String,String>> allTransformMap_valueKey=new HashMap<String,HashMap<String,String>>(); 
	private int inds;
	private double cumErr;
	private double epsilon;
	private double rho;
	private double b;
	private int nsvs;
	private int indDim;
	private Double[]weights;
	private Double[]individuals;
	private int kernelType;
	private int degree;
	private double gamma;
	IDataSourceInfo  dataSourceInfo;
	public int getInds() {
		return inds;
	}
	public void setInds(int inds) {
		this.inds = inds;
	}
	public double getCumErr() {
		return cumErr;
	}
	public void setCumErr(double cumErr) {
		this.cumErr = cumErr;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	public double getRho() {
		return rho;
	}
	public void setRho(double rho) {
		this.rho = rho;
	}
	public double getB() {
		return b;
	}
	public void setB(double b) {
		this.b = b;
	}
	public int getNsvs() {
		return nsvs;
	}
	public void setNsvs(int nsvs) {
		this.nsvs = nsvs;
	}
	public int getIndDim() {
		return indDim;
	}
	public void setIndDim(int indDim) {
		this.indDim = indDim;
	}
	public Double[] getWeights() {
		return weights;
	}
	public void setWeights(Double[] weights) {
		this.weights = weights;
	}
	public Double[] getIndividuals() {
		return individuals;
	}
	public void setIndividuals(Double[] individuals) {
		this.individuals = individuals;
	}
	public SVMModel(DataSet dataSet,DataSet newDataSet) {
		super(dataSet);
		this.newDataSet = newDataSet;
		dataSourceInfo=DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
	}
	public int getKernelType() {
		return kernelType;
	}
	public void setKernelType(int kernelType) {
		this.kernelType = kernelType;
	}

	public StringBuffer generateModelString() {
		StringBuffer str = new StringBuffer();
		if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			str.append("alpine_miner_svm_model_faa");
		}
		str.append("(").append(inds+","+cumErr+","+epsilon+","+rho+","+ b+","+nsvs+","+indDim+",");
		StringBuffer weightsStr = new StringBuffer(); 
		ArrayList<String> weightsArray = new ArrayList<String>();
		if (weights != null){
			for(int i = 0; i < weights.length; i++){
				weightsArray.add(String.valueOf(weights[i]));
			}
			if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
				weightsStr.append(CommonUtility.array2OracleArray(weightsArray,CommonUtility.OracleDataType.Float));
			}else{
				weightsStr.append("array[");
				for(int i = 0; i < weightsArray.size(); i++){
					if(i != 0){
						weightsStr.append(",");
					}
					weightsStr.append(weightsArray.get(i));
				}
				weightsStr.append("]");
			}
		}else{
			weightsStr.append("null");
		}
		str.append(weightsStr).append(",");
		StringBuffer individualsStr = new StringBuffer();
		if (individuals != null){
			ArrayList<String> individualsArray = new ArrayList<String>();
			for(int i = 0; i < individuals.length; i++){
				individualsArray.add(String.valueOf(individuals[i]));
			}
			if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
				individualsStr.append(CommonUtility.array2OracleArray(individualsArray,CommonUtility.OracleDataType.Float));
			}else{
				individualsStr.append("array[");
				for(int j = 0; j < individualsArray.size(); j++){
					if (j != 0){
						individualsStr.append(",");

					}
					individualsStr.append(individualsArray.get(j));
				}
				individualsStr.append("]");
			}

		}else{
			individualsStr.append("null");
		}
		str.append(individualsStr).append(")");
		return str;
	}
	public Array generateWeightsSqlArray(DatabaseConnection databaseConnection) throws SQLException{
		return databaseConnection.getConnection().createArrayOf("DOUBLE", weights);
	}
	public Array generateIndividualsSqlArray(DatabaseConnection databaseConnection) throws SQLException{
		return databaseConnection.getConnection().createArrayOf("DOUBLE", individuals);
	}
	public Array generateColumnsSqlArray(DataSet dataSet, DatabaseConnection databaseConnection) throws SQLException {
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
	 	 					columnsArray.add(caseStr);
 	 					}
 	 				}
			}
		}
		columns = columnsArray.toArray(columns);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		return sqlArray;

	}
	
	
	public StringBuffer generateColumnsString(DataSet dataSet) {
		StringBuffer columnsString = new StringBuffer();
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
	 	 					value = CommonUtility.quoteValue(dataSourceInfo.getDBType(), att,
	 	 							value);
 	 						String caseStr = "(case  when "+columnName+"="+value+" then 1  else 0 end)";
	 	 					columnsArray.add(caseStr);
 	 					}
 	 				}
			}
		}
		if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			columnsString.append(CommonUtility.array2OracleArray(columnsArray,CommonUtility.OracleDataType.Float));
		}else{
			columnsString.append("array[");
			for(int i = 0; i < columnsArray.size(); i++){
				if (i != 0){
					columnsString.append(",");
				}
				columnsString.append(columnsArray.get(i));
			}
			columnsString.append("]");
		}
		return columnsString;
	}

	public StringBuffer getColumnWhere(DataSet dataSet){
		StringBuffer where = new StringBuffer();
		Columns attsNew = dataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		boolean first = true;
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(!first){
				where.append(" and ");
			}else{
				first = false;
			}
			where.append(columnName).append(" is not null ");
		}
		return where;
	}
	public DataSet getNewDataSet() {
		return newDataSet;
	}
	public void setNewDataSet(DataSet newDataSet) {
		this.newDataSet = newDataSet;
	}
	public HashMap<String, HashMap<String, String>> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}
	public void setAllTransformMap_valueKey(
			HashMap<String, HashMap<String, String>> allTransformMapValueKey) {
		allTransformMap_valueKey = allTransformMapValueKey;
	}
	public IDataSourceInfo getDataSourceInfo() {
		return dataSourceInfo;
	}
	public void setDataSourceInfo(IDataSourceInfo dataSourceInfo) {
		this.dataSourceInfo = dataSourceInfo;
	}
	public int getDegree() {
		return degree;
	}
	public void setDegree(int degree) {
		this.degree = degree;
	}
	public double getGamma() {
		return gamma;
	}
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
}
