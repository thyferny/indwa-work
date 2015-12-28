
package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.Tools;
import org.apache.log4j.Logger;

public class SingleARIMAModel implements Serializable {
	
    private static final Logger itsLogger = Logger.getLogger(SingleARIMAModel.class);
    private static final long serialVersionUID = -3496752146166330659L;
	private String dbType;
	private Column idColumn ;
	private Column groupColumn;
	private String idColumnName;
	private String valueColumnName;
	private String groupColumnName;
	private String groupColumnValue;
	private double[]trainLastData;
	private Object[] trainLastIDData;
	private int type;
	private Object interval;
	private int n;
	private int arma[];
	private String idTypeName;
	private String groupTypeName;

	private double[] phi;
	private double[] theta;
	private double [] se;
	private double intercept = Double.NaN;
	private double sigma2; 
	private MakeARIMARet model;
	private int ncxreg ;


	private double likelihood;
	int p,d,q;

	public SingleARIMAModel(DataSet dataSet ,String idColumn, String valueColumn, int p, int d, int q, double[]phi, double[]theta, double intercept,double[] se, double[] data,double[] resid, double mu, int[]arma,MakeARIMARet model, double sigma2, int ncxreg, double likelihood) {
		this.idColumnName = idColumn;
		this.valueColumnName = valueColumn;
		this.p = p;
		this.q = q;
		this.d = d;
		this.phi = phi;
		this.theta = theta;
		this.intercept = intercept;
		this.n = data.length;
		this.se = se;
		this.arma = arma;
		this.model  = model;
		this.sigma2 = sigma2;
		this.ncxreg = ncxreg;
		this.likelihood = likelihood;
	}
	public String toString(){
		String str = "arima("+p+","+d+","+q+"):sigma^2 estimated as "+sigma2+":  part log likelihood = "+likelihood+Tools.getLineSeparator();
		str += "phi:"+ Tools.getLineSeparator();
		for(int i = 0; i < phi.length; i++){
			str += "  ar"+(i+1)+": "+phi[i]+" se: "+se[i]+ Tools.getLineSeparator();
		}
		str += Tools.getLineSeparator();
		str+="theta:"+ Tools.getLineSeparator();
		for(int i = 0; i< theta.length; i++){
			str += "  ma"+(i+1)+": "+theta[i]+" se: "+se[i+phi.length]+ Tools.getLineSeparator();;
		}
		str += Tools.getLineSeparator();
		if(ncxreg > 0){
			str+= "intercept: "+ Tools.getLineSeparator();
			str+="  "+intercept+" se: "+se[se.length - 1]+ Tools.getLineSeparator();;
			str += Tools.getLineSeparator();
		}
		return str;
	}
	public double getSigma2() {
		return sigma2;
	}

	public double getLikelihood() {
		return likelihood;
	}

	public int getP() {
		return p;
	}

	public int getD() {
		return d;
	}

	public int getQ() {
		return q;
	}

	public SingleARIMARPredictResult prediction(int nAhead, Connection connection, String tableName) throws OperatorException{
		try{
		double[] predict = new double[nAhead];
		double[] xm = null;
		if(ncxreg > 0){
			double[] xreg = new double[n];
			for(int i = 0; i < n ; i++){
				xreg[i] = 1.0;
			}
			double[] xregNew = new double[nAhead];
			for(int i = 0; i < nAhead ; i++){
				xregNew[i] = 1.0;
			}
			xm = new double[nAhead];
			for(int i = 0; i < nAhead ; i++){
				xm[i] = xregNew[i] * intercept;
			}
		}
		double [] ma = null;
		if (arma[1] > 0){
			ma = new double [arma[1]];
			for(int i = 0; i < ma.length; i++){
				ma[i] = model.getCoefs()[arma[0]+ i];
			}
		}
		if (arma[3] > 0) {
			ma = new double [arma[3]];
			int numberSum = 0;
			for(int i = 0; i < 3; i++){
				numberSum += arma[i];
			}
			for(int i = 0; i < ma.length; i++){
				ma[i] = model.getCoefs()[numberSum + i];
			}

		}

		KalmanForeRet z = ARIMAR.KalmanForecast(nAhead, model);
		double []se = new double[nAhead];
		for(int i =0 ; i < nAhead; i++){
			predict[i] = z.getForecasts()[i];
			if(ncxreg > 0){
				predict[i] += xm[i];
			}
			se[i] = Math.sqrt(z.getSe()[i] * sigma2);
		}
		Object[] IDData = new Object[nAhead];
		for(int i = 0; i < IDData.length; i++){
			if(idColumn.getValueType()==DataType.DATE ){
				Date id = new Date(((Date)trainLastIDData[trainLastIDData.length - 1]).getTime()+(i+1)*(Long)interval);
				IDData[i] = id;
			}else if(idColumn.getValueType()==DataType.TIME){
				Time id = new Time(((Time)trainLastIDData[trainLastIDData.length - 1]).getTime()+(i+1)*(Long)interval);
				IDData[i] = id;
			}else if(idColumn.getValueType()==DataType.DATE_TIME){
				Timestamp id = new Timestamp(((Timestamp)trainLastIDData[trainLastIDData.length - 1]).getTime()+(i+1)*(Long)interval);
				IDData[i] = id;
			}else if(idColumn.isNumerical()){
				if(idColumn.getValueType() == DataType.INTEGER){
					IDData[i] = (Long)trainLastIDData[trainLastIDData.length - 1] + (i+1)*(Long)interval;
				}else{
					IDData[i] = (Double)trainLastIDData[trainLastIDData.length - 1] + (i+1)*(Double)interval;
				}
			}else{
				IDData[i] = (Long)trainLastIDData[trainLastIDData.length - 1] + (i+1)*(Long)interval;
			}
		}
		storeIntoDatabase(connection, tableName, predict, se, IDData);   
		
//		ARIMARPredictResult result = new ARIMARPredictResult();
		SingleARIMARPredictResult singleResult = new SingleARIMARPredictResult();
		singleResult.setTrainLastData(trainLastData);
		singleResult.setTrainLastIDData(trainLastIDData);
		singleResult.setPredict(predict);
		singleResult.setSe(se);
		singleResult.setIDData(IDData);
		singleResult.setType(type);
		singleResult.setIdColumn(idColumn);
		singleResult.setGroupByValue(getGroupColumnValue());
//		result.getResults().add(singleResult);
		return singleResult;
		}catch(Error e){
			itsLogger.error(e.getMessage(),e);
			if (e instanceof OutOfMemoryError){
				throw new WrongUsedException(null, AlpineAnalysisErrorName.PARA_NOT_APPR_AHEAD);
			}
		}
		return null;
	}
	private void storeIntoDatabase(Connection connection, String tableName, double[] predict,
			double[] se, Object[] IDData) throws OperatorException {
//		String labelValue = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label, labelValue);
//		createTable(connection);
		try {
			connection.setAutoCommit(false);
			Statement st = connection.createStatement();
			for(int i = 0; i < IDData.length; i++){
				String insert = null;
				if(groupColumn == null){
					insert = "insert into "+tableName+" values ("+(idColumn.isNumerical() ? IDData[i].toString() : CommonUtility.quoteValue(dbType, idColumn, idTypeName, IDData[i].toString()))+","+(Double.isNaN(predict[i])?0.0:predict[i])+","+(Double.isNaN(se[i])?0.0:se[i])+")";
				}else{
					insert = "insert into "+tableName+" values ("+(idColumn.isNumerical() ? IDData[i].toString() : CommonUtility.quoteValue(dbType, idColumn, idTypeName, IDData[i].toString()))+","+(groupColumn.isNumerical() ? groupColumnValue : CommonUtility.quoteValue(dbType, groupColumn, groupTypeName ,groupColumnValue))+","+(Double.isNaN(predict[i])?0.0:predict[i])+","+(Double.isNaN(se[i])?0.0:se[i])+")";
				}
				itsLogger.debug("SingleARIMAModel.storeIntoDatabase():sql="+insert);
				st.addBatch(insert);
			}
			st.executeBatch();	
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	public double[] getSe() {
		return se;
	}
	public double[] getPhi() {
		return phi;
	}

	public double[] getTheta() {
		return theta;
	}

	public int getNcxreg() {
		return ncxreg;
	}

	public double getIntercept() {
		return intercept;
	}

	public void setSe(double[] se) {
		this.se = se;
	}
	public double[] getTrainLastData() {
		return trainLastData;
	}

	public void setTrainLastData(double[] trainLastData) {
		this.trainLastData = trainLastData;
	}

	public Object[] getTrainLastIDData() {
		return trainLastIDData;
	}

	public void setTrainLastIDData(Object[] trainLastIDData) {
		this.trainLastIDData = trainLastIDData;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getInterval() {
		return interval;
	}

	public void setInterval(Object interval) {
		this.interval = interval;
	}

	public Column getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(Column idColumn) {
		this.idColumn = idColumn;
	}

	public Column getGroupColumn() {
		return groupColumn;
	}

	public void setGroupColumn(Column groupColumn) {
		this.groupColumn = groupColumn;
	}

	public String getGroupColumnValue() {
		return groupColumnValue;
	}

	public void setGroupColumnValue(String groupColumnValue) {
		this.groupColumnValue = groupColumnValue;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getIdColumnName() {
		return idColumnName;
	}
	public void setIdColumnName(String idColumnName) {
		this.idColumnName = idColumnName;
	}
	public String getValueColumnName() {
		return valueColumnName;
	}
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}
	public String getGroupColumnName() {
		return groupColumnName;
	}
	public void setGroupColumnName(String groupColumnName) {
		this.groupColumnName = groupColumnName;
	}
	public String getIdTypeName() {
		return idTypeName;
	}
	public void setIdTypeName(String idTypeName) {
		this.idTypeName = idTypeName;
	}
	public String getGroupTypeName() {
		return groupTypeName;
	}
	public void setGroupTypeName(String groupTypeName) {
		this.groupTypeName = groupTypeName;
	}

}
