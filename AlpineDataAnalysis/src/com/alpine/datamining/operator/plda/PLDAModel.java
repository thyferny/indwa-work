


package com.alpine.datamining.operator.plda;

import java.sql.Connection;
import java.sql.SQLException;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;
import org.apache.log4j.Logger;


public abstract class PLDAModel extends Prediction{

	
	private static final long serialVersionUID = 3406102217588177652L;

	
	protected PLDAModel(DataSet trainingDataSet) {
		super(trainingDataSet);
		// TODO Auto-generated constructor stub
	}

    private static final Logger logger = Logger.getLogger(PLDAModel.class);
    

    protected static String PLDAPreString="PLDA";
	private String modelSchema;
	private String modelTable;
	private String dictSchema;
	private String dictTable;
	private double alpha;
	private double beta;
	private String dicIdColumn;
	private String dicContentColumn;
	private String docIdColumn;
	private String docContentColumn ; 
	private long topicNumber;
	
	private String dbsystem;
	private String url;
	private String userName;
	private String password;
	
	
	
	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDicIdColumn() {
		return dicIdColumn;
	}


	public void setDicIdColumn(String dicIdColumn) {
		this.dicIdColumn = dicIdColumn;
	}


	public String getDicContentColumn() {
		return dicContentColumn;
	}


	public void setDicContentColumn(String dicContentColumn) {
		this.dicContentColumn = dicContentColumn;
	}


	public String getDocIdColumn() {
		return docIdColumn;
	}


	public void setDocIdColumn(String docIdColumn) {
		this.docIdColumn = docIdColumn;
	}


	public String getDocContentColumn() {
		return docContentColumn;
	}


	public void setDocContentColumn(String docContentColumn) {
		this.docContentColumn = docContentColumn;
	}


	public long getTopicNumber() {
		return topicNumber;
	}

	public void setTopicNumber(long topicNumber) {
		this.topicNumber = topicNumber;
	}

	
	public String getDictTable() {
		return dictTable;
	}

	public void setDictTable(String dictTable) {
		this.dictTable = dictTable;
	}

	public String getModelTable() {
		return modelTable;
	}

	public void setModelTable(String modelTable) {
		this.modelTable = modelTable;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}


	
	public String getDbsystem() {
		return dbsystem;
	}


	public void setDbsystem(String dbsystem) {
		this.dbsystem = dbsystem;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	
	public String getModelSchema() {
		return modelSchema;
	}


	public void setModelSchema(String modelSchema) {
		this.modelSchema = modelSchema;
	}


	public String getDictSchema() {
		return dictSchema;
	}


	public void setDictSchema(String dictSchema) {
		this.dictSchema = dictSchema;
	}


	public abstract void  PLDAPredict(String predictTable, long iterationNumber,
			String docOutSchema, String docOutTable, String appendOnlyString, String endingString, String topicOutSchema,
			String topicOutTable,String docTopicAppendOnlyString, String docTopicEndingString, Connection conncetion, String dropIfExist,
			String dropIfExistRTable) throws SQLException ;
	
	
}
