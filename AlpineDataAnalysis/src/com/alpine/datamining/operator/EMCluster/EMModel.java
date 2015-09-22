 /**
 * 

* ClassName EMModel.java
*
* Version information: 1.00
*
* Data: Jul 30, 2012
*
* COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.

 */


package com.alpine.datamining.operator.EMCluster;
/**
 * @author Shawn
 *
 *  
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.training.Prediction;
import org.apache.log4j.Logger;

public abstract class EMModel extends Prediction{


	private static final long serialVersionUID = -3542387906420861259L;
	protected static String AlpineMinerEMClusterTable="alpine_emtemp_";
	protected static String AlpineMinerEMColumnTable="alpine_coltemp_";
	private AnalysisStorageParameterModel EMModelOutputTableStorageParameters;
	private HashMap<String,String> allTransformMap_valueKey=new HashMap<String,String>(); 

	public HashMap<String,String> getAllTransformMap_valueKey() {
		return allTransformMap_valueKey;
	}


	public void setAllTransformMap_valueKey(
			HashMap<String,String> allTransformMap_valueKey) {
		this.allTransformMap_valueKey = allTransformMap_valueKey;
	}


	public AnalysisStorageParameterModel getEMModelOutputTableStorageParameters() {
		return EMModelOutputTableStorageParameters;
	}


	public void setEMModelOutputTableStorageParameters(
			AnalysisStorageParameterModel eMModelOutputTableStorageParameters) {
		EMModelOutputTableStorageParameters = eMModelOutputTableStorageParameters;
	}


	protected EMModel(DataSet trainingDataSet) {
		super(trainingDataSet);
	}

	

	private int clusterNumber;
	


	private Map<Integer,EMClusterNode> clusteInfo=new HashMap<Integer,EMClusterNode>();
	
	
	
	
	public int getClusterNumber() {
		return clusterNumber;
	}


	public void setClusterNumber(int clusterNumber) {
		this.clusterNumber = clusterNumber;
	}



	 
	public Map<Integer,EMClusterNode> getClusteInfo() {
		return clusteInfo;
	}





	public void setClusteInfo(Map<Integer,EMClusterNode> clusteInfo) {
		this.clusteInfo = clusteInfo;
	}
	
	public EMClusterNode getClusteSingleValue(Integer clusterNumber) {
		return clusteInfo.get(clusterNumber);
	}

	public void addClusteSingleValue(Integer clusterNumber,EMClusterNode clusteInfo) {
		this.clusteInfo.put(clusterNumber, clusteInfo);
	}
	




	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		return null;
	}


	public abstract void EMClusterPredict(String predictTable, String appendOnlyString,
			String endingString, Connection conncetion, String dropIfExist, String schemaName, String tableName) throws SQLException ;

}
