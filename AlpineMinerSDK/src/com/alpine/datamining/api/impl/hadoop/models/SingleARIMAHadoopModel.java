package com.alpine.datamining.api.impl.hadoop.models;

/**
 * ClassName SingleARIMAHadoopModel Model
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

import java.io.Serializable;
import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.impl.hadoop.predictor.HadoopSingleARIMARPredictResult;
import com.alpine.datamining.api.impl.hadoop.trainer.HadoopARIMATrainer;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.timeseries.KalmanForeRet;
import com.alpine.datamining.operator.timeseries.MakeARIMARet;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.Tools;
/**
 * The model of the improved neural net.
 * 
 */
public class SingleARIMAHadoopModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3496752146166330659L;
	private String dbType;
	private Column idColumn ;
	private Column groupColumn;
	private String idColumnName;
	private String valueColumnName;
	private String groupColumnName;
	private String groupColumnValue;
	private double[] trainLastData;
	private double[] trainLastIDData;
	private int type;
	private long interval;
	private int n;
	private int arma[];
	private String idTypeName;
	private String groupTypeName;
	private String formatType="Integer";

	public String getFormatType() {
		return formatType;
	}
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}
	private double[] phi;
	private double[] theta;
	private double [] se;
	private double intercept = Double.NaN;
	private double sigma2; 
	private MakeARIMARet model;
	private int ncxreg ;


	private double likelihood;
	int p,d,q;
	
	private static Logger itsLogger = Logger
			.getLogger(SingleARIMAHadoopModel.class);

	public SingleARIMAHadoopModel(String idColumn, String valueColumn, int p, int d, int q, double[]phi, double[]theta, double intercept,double[] se, double[] data,double[] resid, double mu, int[]arma,MakeARIMARet model, double sigma2, int ncxreg, double likelihood) {
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
	public SingleARIMAHadoopModel(String idColumn2, String valueColumn,
			int parseInt, int parseInt2, int parseInt3, double[] bestPhi,
			double[] bestTheta, double intercept2, double[] varCoef,
			double[] data, double[] residuals, int mu, int[] arma2,
			com.alpine.hadoop.timeseries.MakeARIMARet model2, double sigma22,
			int ncxreg2, double likelihood2) {
		// TODO Auto-generated constructor stub
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

	public HadoopSingleARIMARPredictResult prediction(int nAhead) throws Exception{
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

		KalmanForeRet z = HadoopARIMATrainer.KalmanForecast(nAhead, model);
		double[] se = new double[nAhead];
		for(int i =0 ; i < nAhead; i++){
			predict[i] = z.getForecasts()[i];
			if(ncxreg > 0){
				predict[i] += xm[i];
			}
			se[i] = Math.sqrt(z.getSe()[i] * sigma2);
		}
		Object[] IDData = new Object[nAhead];
		if("Integer".equalsIgnoreCase(formatType)){
			for(int i = 0; i < IDData.length; i++){
				IDData[i] = (double) (trainLastIDData[trainLastIDData.length - 1] + (i+1)*(Long)interval);
			}
			type=Types.INTEGER;
		}
		else{
			for(int i = 0; i < IDData.length; i++){
				IDData[i]=new Date(((Double)(trainLastIDData[trainLastIDData.length - 1] + (i+1)*(Long)interval)).longValue());
			}
			type=Types.DATE;
		}
		Object[] oIDData=new Object[trainLastIDData.length];
		if("Integer".equalsIgnoreCase(formatType)){
			for(int i=-0;i<oIDData.length;i++){
				oIDData[i]=trainLastIDData[i];
			}
		}
		else{
			for(int i=-0;i<oIDData.length;i++){
				oIDData[i]=new Date(((Double)trainLastIDData[i]).longValue());
			}
		}
		HadoopSingleARIMARPredictResult singleResult = new HadoopSingleARIMARPredictResult();
		singleResult.setTrainLastData(trainLastData);
		singleResult.setTrainLastIDData(oIDData);
		singleResult.setPredict(predict);
		singleResult.setSe(se);
		singleResult.setIDData(IDData);
		singleResult.setType(type);
		singleResult.setIdColumn(idColumnName);
		singleResult.setGroupByValue(getGroupColumnValue());
//		result.getResults().add(singleResult);
		return singleResult;
		}catch(Error e){
			itsLogger.debug(e);
			if (e instanceof OutOfMemoryError){
				throw new WrongUsedException(null, AlpineAnalysisErrorName.PARA_NOT_APPR_AHEAD);
			}
		}
		return null;
	}
//	private void storeIntoDatabase(Connection connection, String tableName, double[] predict,
//			double[] se, Object[] IDData) throws OperatorException {
////		String labelValue = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label, labelValue);
////		createTable(connection);
//		try {
//			connection.setAutoCommit(false);
//			Statement st = connection.createStatement();
//			for(int i = 0; i < IDData.length; i++){
//				String insert = null;
//				if(groupColumn == null){
//					insert = "insert into "+tableName+" values ("+(idColumn.isNumerical() ? IDData[i].toString() : CommonUtility.quoteValue(dbType, idColumn, idTypeName, IDData[i].toString()))+","+(Double.isNaN(predict[i])?0.0:predict[i])+","+(Double.isNaN(se[i])?0.0:se[i])+")";
//				}else{
//					insert = "insert into "+tableName+" values ("+(idColumn.isNumerical() ? IDData[i].toString() : CommonUtility.quoteValue(dbType, idColumn, idTypeName, IDData[i].toString()))+","+(groupColumn.isNumerical() ? groupColumnValue : CommonUtility.quoteValue(dbType, groupColumn, groupTypeName ,groupColumnValue))+","+(Double.isNaN(predict[i])?0.0:predict[i])+","+(Double.isNaN(se[i])?0.0:se[i])+")";
//				}
//				itsLogger.debug("SingleARIMAModel.storeIntoDatabase():sql="+insert);
//				st.addBatch(insert);
//			}
//			st.executeBatch();	
//			connection.commit();
//			connection.setAutoCommit(true);
//		} catch (SQLException e) {
//			throw new OperatorException(e.getLocalizedMessage());
//		}
//	}

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

	public double[] getTrainLastIDData() {
		return trainLastIDData;
	}

	public void setTrainLastIDData(double[] id) {
		this.trainLastIDData = id;
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

	public void setInterval(long interval) {
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

