package com.alpine.datamining.operator.timeseries;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;

public class ARIMAR extends Trainer {
	private static final Logger logger = Logger.getLogger(ARIMAR.class); 
	private ARIMARParameter para;
	private String idColumn;
	private String valueColumn;

	private int p = 0, q = 0, d = 0;
	private double [] data;
	private double[] bestPhi;
	private double[] bestTheta;

	private double sigma2; 
	private double likelihood;
	private int[] arma;
	private int ncxreg  = 0 ;
	private double[] trainLastData;
	private Object[] idData;
	private Object interval;
	private String groupColumnName;
	private int groupNumber = 1;
	private int groupIndex = 0;
	private	List<String> groupList = new ArrayList<String>();
	private String idTypeName;
	private String groupTypeName;
	private int idType;
	private int groupType;
	private Column groupColumn;
	private IDataSourceInfo dataSourceInfo;


	public ARIMAR() {
		super();
	}


	private ARIMARet arima() throws WrongUsedException
	{
		double[] x = data;
		int[]order = new int[]{p,d,q};
		int[]seasonalOrder = new int[]{0,0,0};
		int seasonalPeriod = 1;
		int n = x.length;
		arma = new int[]{order[0],order[2],seasonalOrder[0],seasonalOrder[2],seasonalPeriod,
				order[1],seasonalOrder[1]};
		int narma = 0;
		for(int i = 0; i < 4; i++){
			narma += arma[i];
		}
		double[]delta = new double[1];
		delta[0] = 1;
		double[] tmp = new double[]{1,-1};
		for(int i = 0; i < order[1]; i++){
			delta = TSconv(delta, tmp);
		}
		tmp = new double[2+seasonalPeriod - 1];
		tmp[0] = 1;
		tmp[tmp.length - 1]= -1;
		for(int i =0; i < seasonalPeriod - 1; i++){
			tmp[i] = 0;
		}
		for(int i = 0; i < seasonalOrder[1]; i++){
			delta = TSconv(delta, tmp);
		}
		double[] deltaOld = delta;
		delta = new double[deltaOld.length - 1];
		for(int i = 0; i < delta.length; i++){
			delta[i] = -deltaOld[i + 1];
		}
	    int nUsed = n;

		nUsed = n - delta.length;
		int nd = order[1] + seasonalOrder[1];
		ncxreg = 0;
		boolean includeMean = true;//false;
		double [] xreg = null;
		if (includeMean && (nd == 0)) {
		    xreg = new double[n];
		    for(int i = 0; i < n ; i++){
		    	xreg[i] = 1;
		    }
		    ncxreg = ncxreg + 1;
		}

		int ncond = order[1] + seasonalOrder[1] * seasonalPeriod;
	    int ncond1 = order[0] + seasonalPeriod * seasonalOrder[0];

	    Integer nCond = null;

	    if (nCond != null) 
	    	ncond  = ncond + Math.max(nCond, ncond1);
	    else 
	    	ncond = ncond + ncond1;
	    double[] fixed = new double[narma + ncxreg];
	    for(int i = 0; i < fixed.length; i++){
	    	fixed[i] = Double.NaN;
	    }
	    boolean [] mask = new boolean[fixed.length];
	    for(int i = 0; i < mask.length; i++){
	    	if (Double.isNaN(fixed[i])){
	    		mask[i] = true;
	    	}else{
	    		mask[i] = false;
	    	}
	    }
	    boolean noOptim = false;

	    double[] init0 = new double[narma];
	    double[] parscale = new double[narma];
	    for(int i = 0; i < parscale.length; i++){
	    	parscale[i] = 1;
	    }
	    
	    if (ncxreg != 0) {
	        double[][] lmxreg = new double[1][];
	        lmxreg[0] = new double[xreg.length];
	        for(int i = 0; i < lmxreg[0].length; i++){
	        	lmxreg[0][i] = xreg[i];
	        }
	        int lmp = lmxreg.length;
	        double [][]lmy = new double[1][];
	        lmy[0] = new double[x.length];
	        System.arraycopy(x, 0, lmy[0],0,lmy[0].length);
	        int lmny = lmy.length;

	        double [][] work = new double[2][lmp];

	        double[][]b = new double[lmny][lmp];
	        double[][]residuals = new double[lmy.length][lmy[0].length];
	        double[][]effects = new double[lmy.length][lmy[0].length];
	        double[]qraux = new double[lmp];
	        for(int i = 0; i < lmy.length; i++){
	        	System.arraycopy(lmy[i], 0, residuals[i],0,residuals[i].length);
	        	System.arraycopy(lmy[i], 0, effects[i],0,effects[i].length);
	        }
	        int []jpvt = new int[lmp];
	        for(int i = 0; i < jpvt.length; i++){
	        	jpvt[i] = i+1;
	        }
			LMRet lmRet = LM.dqrls(lmxreg , n, 1, lmy, 1, 1e-07, b ,residuals, effects, 1, jpvt, qraux, work);       

			//	        n.used <- sum(!is.na(resid(fit))) - length(Delta)
			double coefFit = lmRet.getCoefficient()[0];
			double[] init0New = new double[init0.length + 1];
			System.arraycopy(init0, 0, init0New, 0, init0.length);
			
			init0New[init0.length] = coefFit;
			init0= init0New;
			double ses = lmRet.getSes()[0];
			double [] parscaleNew = new double[parscale.length + 1];
			System.arraycopy(parscale, 0, parscaleNew, 0, parscale.length);
			parscaleNew[parscale.length] = 10*ses;
			parscale = parscaleNew;
	    }
	    
	    double[] init = init0;
	    double [] coef = fixed;
	    XReg xReg = new XReg();
	    xReg.setNcxreg(ncxreg);
	    xReg.setNarma(narma);
	    xReg.setXreg(xreg);
	    if (ncond >= n){
			throw new WrongUsedException(this,AlpineAnalysisErrorName.DATASET_TOO_SMALL);
	    }
	    OptimResHessRet res = Optimization.optim(init, 
				arma,
				ncond,
	    		true,
	    		x,
	    		xReg,
	    		parscale); 
	    
        coef = res.getRes().getPar();
        TransParsRet trarma = ARIMA_transPars(coef, arma, false);
        double kappa= 1e6;
		MakeARIMARet mod = makeARIMA(coef,trarma.getsPhi(), trarma.getsTheta(), delta, kappa);
        if(ncxreg > 0) {
        	for (int i = 0; i < x.length; i++){
        		x[i] = x[i] - xreg[i] * coef[narma];
        	}
        }
		double [] P = new double[mod.getP().length * mod.getP()[0].length];
		for(int i = 0; i < mod.getP().length;  i++){
			for (int j = 0; j < mod.getP()[0].length; j++){
				P[i*mod.getP()[0].length + j] = mod.getP()[i][j];
			}
		}
		double [] Pn = new double[mod.getPn().length * mod.getPn()[0].length];
		for(int i = 0; i < mod.getPn().length;  i++){
			for (int j = 0; j < mod.getPn()[0].length; j++){
				Pn[i*mod.getPn()[0].length + j] = mod.getPn()[i][j];
			}
		}

		ARIMA_Like(x, mod.getPhi(), mod.getTheta(), mod.getDelta(),mod.getA(),P,Pn,0,true);
				
		CSSRet val = 
		ARIMA_CSS(x, arma,trarma,
				ncond, true);
		sigma2 = val.getValue();
		Matrix var = null;
		if(noOptim){
//			var = 0;
		}else{
			try {
				var = res.getHess().times(nUsed).SVDInverse();
			} catch (OperatorException e) {
				logger.error("svd exception:"+e.getLocalizedMessage());
			}
		}
		double[] varCoef = new double[coef.length];
		String varString = new String();
		if(var != null){
			for(int i = 0; i < var.getColumnDimension(); i++){
					varCoef[i] = Math.sqrt(var.get(i,i));
					varString+=","+varCoef[i];
			}
		}
		double value  = 2 * nUsed * res.getRes().getValue() + nUsed + nUsed * Math.log(2 * Math.PI);
	    double aic = Double.NaN;

	    ARIMARet aRIMARes = new ARIMARet();
	    aRIMARes.setCoef(coef);
	    aRIMARes.setSigma2(sigma2);
	    aRIMARes.setLoglik(-0.5 * value);
	    likelihood = aRIMARes.getLoglik() ;
	    aRIMARes.setAic(aic);
	    aRIMARes.setArma(arma);
	    aRIMARes.setX(x) ;
	    aRIMARes.setnCond(ncond);
	    aRIMARes.setModel(mod);
	    aRIMARes.setVarCoef(varCoef);
	    aRIMARes.setResiduals(val.getsResid());
	    return aRIMARes;
	}
	@Override
	public Model train(DataSet dataSet) throws OperatorException {
		para = (ARIMARParameter)getParameter();
        this.dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		ARIMAModel model = new ARIMAModel(dataSet);
		model.setDbType(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
		groupColumn = dataSet.getColumns().get(para.getGroupColumn());
		groupColumnName = StringHandler.doubleQ(para.getGroupColumn());
		p = para.getP();
		q = para.getQ();
		d = para.getD();
		getIdGroupType(dataSet);
		try{
			if(StringUtil.isEmpty(para.getGroupColumn())){
				groupNumber = 1;
			}else{
				
//				groupNumber = dataSet.getcolumns().get(para.getGroupColumn()).getMapping().size();
				getGroupNumber(dataSet);
			}
			model.setIdColumn(dataSet.getColumns().get(para.getIdColumn()));
			model.setIdColumnName(para.getIdColumn());
			model.setValueColumnName(para.getValueColumn());
			model.setIdSqlType(idType);
			model.setIdTypeName(idTypeName);
			if(!StringUtil.isEmpty(para.getGroupColumn())){
				model.setGroupColumn(dataSet.getColumns().get(para.getGroupColumn()));
				model.setGroupColumnName(para.getGroupColumn());
				model.setGroupSqlType(groupType);
				model.setGroupTypeName(groupTypeName);
			}
			for(groupIndex = 0; groupIndex < groupNumber; groupIndex++){
				getData(dataSet);
				SingleARIMAModel singleModel = singleTrain(dataSet);
				singleModel.setIdColumn(dataSet.getColumns().get(para.getIdColumn()));
				singleModel.setIdColumnName(para.getIdColumn());
				singleModel.setValueColumnName(para.getValueColumn());
				singleModel.setIdTypeName(idTypeName);
				singleModel.setDbType(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
				if(!StringUtil.isEmpty(para.getGroupColumn())){
					singleModel.setGroupColumn(dataSet.getColumns().get(para.getGroupColumn()));
					singleModel.setGroupColumnName(para.getGroupColumn());
					singleModel.setGroupColumnValue(groupList.get(groupIndex));
					singleModel.setGroupTypeName(groupTypeName);
				}else{
					singleModel.setGroupColumnValue(null);
				}
				singleModel.setGroupColumn(dataSet.getColumns().get(para.getGroupColumn()));
				model.getModels().add(singleModel);
			}
			return model;
		}catch(Error e){
			logger.error(e);
			if (e instanceof OutOfMemoryError){
				throw new WrongUsedException(this, AlpineAnalysisErrorName.PARA_NOT_APPR_ORDER);
			}
		}
		return model;
	}
	private SingleARIMAModel singleTrain(DataSet dataSet) throws WrongUsedException{
//		X = new Matrix(n,p+q);
		bestPhi = new double[p];
		bestTheta = new double[q];
		ARIMARet res = arima();
		for(int i = 0; i < p; i++){
			bestPhi[i] = res.getCoef()[i];
		}
		for(int i = 0; i < q; i++){
			bestTheta[i] = res.getCoef()[p+i];
		}
		double intercept = Double.NaN; 
		if(ncxreg > 0)
		{
			intercept = res.getCoef()[res.getCoef().length - 1];
		}
		SingleARIMAModel singleModel = new SingleARIMAModel(dataSet ,idColumn, valueColumn, p, d, q, bestPhi, bestTheta, intercept,res.getVarCoef(),data,res.getResiduals(),0,arma,res.getModel(),sigma2,ncxreg,likelihood);
		singleModel.setTrainLastData(trainLastData);
		singleModel.setTrainLastIDData(idData);
		singleModel.setType(idType);
		singleModel.setInterval(interval);
		singleModel.setIdColumn(dataSet.getColumns().get(para.getIdColumn()));
		return singleModel;
	}
	private void getIdGroupType(DataSet dataSet) throws OperatorException{
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		String tableName=((DBTable) dataSet.getDBTable()).getTableName();
		StringBuffer sql = new StringBuffer();
		if(StringUtil.isEmpty(para.getGroupColumn())){
			sql.append("select ").append(StringHandler.doubleQ(para.getIdColumn())).append(" from ").append(tableName).append(" where 1 = 0");
		}else{
			sql.append("select ").append(StringHandler.doubleQ(para.getIdColumn())).append(",").append(StringHandler.doubleQ(para.getGroupColumn())).append(" from ").append(tableName).append(" where 1 = 0");
		}
		Statement st = null;

		try {
			st = databaseConnection.createStatement(false);
			ResultSet rs = st.executeQuery(sql.toString());
			idType = rs.getMetaData().getColumnType(1);
			idTypeName = rs.getMetaData().getColumnTypeName(1);
			if(!StringUtil.isEmpty(para.getGroupColumn())){
				groupType = rs.getMetaData().getColumnType(2);
				groupTypeName = rs.getMetaData().getColumnTypeName(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private void getGroupNumber(DataSet dataSet) throws OperatorException{
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		
		String tableName=((DBTable) dataSet.getDBTable()).getTableName();
		Statement st = null;

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		int threshold = para.getThreshold();
		if(threshold > AlpineDataAnalysisConfig.ARIMA_MAX_COUNT){
			threshold = AlpineDataAnalysisConfig.ARIMA_MAX_COUNT;
		}
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("select distinct ").append(groupColumnName).append(" from ").append(tableName).append(" where ").append(groupColumnName).append(" is not null order by ").append(groupColumnName);
			logger.debug("ARIMA.getData():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());			
			while(rs.next()) {
				String groupString = rs.getString(1);
				groupList.add(groupString);
			}
			groupNumber = groupList.size();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}finally{
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}
	private double[] getData(DataSet dataSet) throws OperatorException{
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		
		String tableName=((DBTable) dataSet.getDBTable()).getTableName();
		Statement st = null;

		String idColumn = StringHandler.doubleQ(para.getIdColumn());
		String valueColumn = StringHandler.doubleQ(para.getValueColumn());
		isDistinct(databaseConnection, idColumn, tableName);
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		int threshold = para.getThreshold();
		if(threshold > AlpineDataAnalysisConfig.ARIMA_MAX_COUNT){
			threshold = AlpineDataAnalysisConfig.ARIMA_MAX_COUNT;
		}
		long dataCount = 0;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("select count(*) from ").append(tableName).append(" where ").append(idColumn).append(" is not null and ").append(valueColumn).append(" is not null ");
			if(!StringUtil.isEmpty(para.getGroupColumn())){
				sql.append(" and ").append(groupColumnName).append(" = "+CommonUtility.quoteValue(dataSourceInfo.getDBType(),groupColumn,groupList.get(groupIndex)));
			}
			logger.debug("ARIMA.getData():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());			
			if(rs.next()) {
				dataCount = rs.getLong(1);
			}
			if(dataCount <= 1){
				if(!StringUtil.isEmpty(para.getGroupColumn())){
					throw new WrongUsedException(this, AlpineAnalysisErrorName.ARIMA_DATASET_TOO_SAMLL_GROUP, groupList.get(groupIndex));
				}else{
					throw new WrongUsedException(this, AlpineAnalysisErrorName.ARIMA_DATASET_TOO_SAMLL);
				}
			}
			data = retrieveData(dataSet, tableName, st, idColumn, valueColumn,
					threshold, dataCount);
			trainLastData = retrieveData(dataSet, tableName, st, idColumn, valueColumn,
					AlpineDataAnalysisConfig.ARIMA_LAST_DATA_COUNT, dataCount);
			idData = getIDData(dataSet, tableName,
					st, idColumn, valueColumn, AlpineDataAnalysisConfig.ARIMA_LAST_DATA_COUNT,
					dataCount);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}finally{
			try {
				st.close();
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		return data;
	}

	private double[] retrieveData(DataSet dataSet, String tableName,
			Statement st, String idColumn, String valueColumn, int threshold,
			long dataCount) throws SQLException {
		double[] data;
		if (dataCount > threshold){
			data = new double[threshold];
		}else{
			data = new double[(int)dataCount];
		}

		StringBuffer sql;
		ResultSet rs;
		sql = new StringBuffer();
		sql.append("select ").append(valueColumn).append(" from ").append(tableName).append(" where ").append(idColumn).append(" is not null and ").append(valueColumn).append(" is not null ");
		if(!StringUtil.isEmpty(para.getGroupColumn())){
			sql.append(" and ").append(groupColumnName).append(" = "+CommonUtility.quoteValue(dataSourceInfo.getDBType(),groupColumn,groupList.get(groupIndex)));
		}
		sql.append(" order by ").append(idColumn);
		if( dataCount > threshold){
			if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				sql.insert(0, " select  rownum alpinerownum , "+valueColumn+" from ( ");
				sql.insert(0, "select "+valueColumn+" from(");
				sql.append("  ) foo ) where alpinerownum > ").append(dataCount - threshold);
			}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				sql.insert(0, " select  row_number() over() as alpinerownum , "+valueColumn+" from ( ");
				sql.insert(0, "select "+valueColumn+" from(");
				sql.append("  ) foo ) where alpinerownum > ").append(dataCount - threshold);
			}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				sql.append(" offset  ").append(dataCount - threshold);
			}else{
				sql.append(" offset  ").append(dataCount - threshold);
			}
		}
		logger.debug("ARIMAR.retrieveData():sql="+sql);
		rs = st.executeQuery(sql.toString());
		int i = 0;
		while (rs.next()) {
			double value = rs.getDouble(1);
			data[i] = value;
			i++;
		}
		rs.close();
		return data;
	}

	private Object[] getIDData(DataSet dataSet, String tableName,
			Statement st, String idColumnName, String valueColumn, int threshold,
			long dataCount) throws SQLException {
		Object[] data;
		int length = 0;
		if (dataCount > threshold){
			length = threshold;
		}else{
			length = (int)dataCount;
		}

		StringBuffer sql;
		ResultSet rs;
		sql = new StringBuffer();
		sql.append("select ").append(idColumnName).append(" from ").append(tableName).append(" where ").append(idColumnName).append(" is not null and ").append(valueColumn).append(" is not null ");
		if(!StringUtil.isEmpty(para.getGroupColumn())){
			sql.append(" and ").append(groupColumnName).append(" = "+CommonUtility.quoteValue(dataSourceInfo.getDBType(),groupColumn,groupList.get(groupIndex)));
		}
		sql.append(" order by ").append(idColumnName);
		if( dataCount > threshold){
			if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
				sql.insert(0, " select  rownum alpinerownum , "+idColumnName+" from ( ");
				sql.insert(0, "select "+idColumnName+" from(");
				sql.append("  ) foo ) where alpinerownum > ").append(dataCount - threshold);
			}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				sql.insert(0, " select  row_number() over() as alpinerownum , "+idColumnName+" from ( ");
				sql.insert(0, "select "+idColumnName+" from(");
				sql.append("  ) foo ) where alpinerownum > ").append(dataCount - threshold);
			}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				sql.append(" offset  ").append(dataCount - threshold);
			}else{
				sql.append(" offset  ").append(dataCount - threshold);
			}
		}
		logger.debug("ARIMAR.getIDData():sql="+sql);
		rs = st.executeQuery(sql.toString());
		int i = 0;
		Column idColumn = dataSet.getColumns().get(para.getIdColumn()); 
		data = new Object[length];
		Object[] intervalArray = new Object[data.length - 1];
		while (rs.next()) {
			if(idColumn.getValueType()==DataType.DATE){
				data[i] = rs.getDate(1);
				if(i > 0){
					intervalArray[i - 1] =((Date)data[i]).getTime() - ((Date)data[i - 1]).getTime(); 
				}
			}else if(idColumn.getValueType()==DataType.TIME){
				data[i] = rs.getTime(1);
				if(i > 0){
					intervalArray[i - 1] =((Time)data[i]).getTime() - ((Time)data[i - 1]).getTime(); 
				}
			}else if(idColumn.getValueType()==DataType.DATE_TIME){
				data[i] = new Timestamp(rs.getTimestamp(1).getTime());
				if(i > 0){
					intervalArray[i - 1] =((Timestamp)data[i]).getTime() - ((Timestamp)data[i - 1]).getTime(); 
				}
			}else if(idColumn.isNumerical()){
				if(idColumn.getValueType() == DataType.INTEGER){
					data[i] = (Long)rs.getLong(1);
					if(i > 0){
						intervalArray[i - 1] =((Long)data[i]) - ((Long)data[i - 1]); 
					}
				}else{
					data[i] = (Double)rs.getDouble(1);
					if(i > 0){
						intervalArray[i - 1] =((Double)data[i]) - ((Double)data[i - 1]); 
					}
				}
			}else{
				data[i] = Long.valueOf(String.valueOf(i - length));
				if(i > 0){
					intervalArray[i - 1] = ((Long)data[i]) - ((Long)data[i - 1]); 
				}
			}
			i++;
		}
		
		Map<Object, Integer> intervalMap = new HashMap<Object, Integer>();
		for(int j = 0; j < intervalArray.length; j++){
			if (intervalMap.containsKey(intervalArray[j])){
				intervalMap.put(intervalArray[j], intervalMap.get(intervalArray[j]) + 1);
			}else{
				intervalMap.put(intervalArray[j], 1);
			}
		}

		Iterator<Entry<Object, Integer>> it = intervalMap.entrySet().iterator();  

		int max = 0;
		while (it.hasNext()) {
		        Map.Entry<Object, Integer> entry = it.next();
		        Object key = entry.getKey();
		        Integer value = entry.getValue();
		        if (value > max){
		        	max = value;
		        	interval = key;
		        }
		}
		rs.close();
		return data;
	}
	private void isDistinct(DatabaseConnection databaseConnection, String id, String tableName) throws OperatorException {
		try {
			Statement st = databaseConnection.createStatement(false);
			String sql = "select count(" + id+ "),count(distinct " + id
					+ ") from "
					+ tableName;
			if(!StringUtil.isEmpty(para.getGroupColumn())){
				sql += " where "+groupColumnName+" = "+CommonUtility.quoteValue(dataSourceInfo.getDBType(),groupColumn,groupList.get(groupIndex));
			}
			logger.debug("ARIMAR.isDistinct():sql="+sql);
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()){
				if (!rs.getBigDecimal(1).equals(rs.getBigDecimal(2))) {
					logger.error("ID column Must be distinct");
					throw new WrongUsedException(this, AlpineAnalysisErrorName.ID_NOT_DISTINCT);
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	static CSSRet
	ARIMA_CSS(double[]  sy, int[]  sarma, TransParsRet trarma,
			int sncond, boolean  giveResid)
	{
		double[]   sResid;
	    double ssq = 0.0; double []y = (sy);double tmp;
	    double []phi = (trarma.getsPhi());double []theta = (trarma.getsTheta()); double[]w; double[]resid;
	    int n = (sy.length);int[] arma = (sarma); int p = (trarma.getsPhi().length);
	        int q = (trarma.getsTheta().length); int ncond = (sncond);
	    int l, i, j, ns, nu = 0;
	    boolean useResid = (giveResid);

	    w = new double[n];
	    for (l = 0; l < n; l++) w[l] = y[l];
	    for (i = 0; i < arma[5]; i++)
	        for (l = n - 1; l > 0; l--) w[l] -= w[l - 1];
	    ns = arma[4];
	    for (i = 0; i < arma[6]; i++)
	        for (l = n - 1; l >= ns; l--) w[l] -= w[l - ns];
	    sResid = new double[n];
	    resid = (sResid);
	    if (useResid) for (l = 0; l < ncond && l < n; l++) resid[l] = 0;

	    for (l = ncond; l < n; l++) {
	        tmp = w[l];
	        for (j = 0; j < p; j++) tmp -= phi[j] * w[l - j - 1];
	        for (j = 0; j < Math.min(l - ncond, q); j++)
	            tmp -= theta[j] * resid[l - j - 1];
	        resid[l] = tmp;
	        if (!Double.isNaN(tmp)) {
	            nu++;
	            ssq += tmp * tmp;
	        }
	    }
	    CSSRet res = new CSSRet();
	    res.setUseResid(useResid);
	    if (useResid) {
	        res.setValue(ssq/nu);
	        res.setsResid(sResid);
	        return res;
	    } else {
	    	res.setValue(ssq/nu);
	    }
	    return res;
	}

    static double armaCSS (double[]x, double[]p, double[] fixed, int[] arma, int ncond,XReg xReg)
    {
        double[] par = (fixed);
        par = p;
        TransParsRet trarma = ARIMA_transPars(par, arma, false);
        double[]xNew = new double[x.length];
        System.arraycopy(x, 0, xNew, 0, xNew.length);
        if(xReg.getNcxreg() > 0){
        	for(int i = 0; i < x.length; i++){
        		xNew[i] = x[i] - xReg.getXreg()[i] * par[xReg.getNarma()];
        	}
        }
        CSSRet res = null;
        res = ARIMA_CSS( xNew, arma, trarma,
                     ncond, false);
        return 0.5 * Math.log(res.getValue());
    }
   
    static KalmanForeRet KalmanForecast( int nAhead, MakeARIMARet mod)
    {
        double[]a = null;
        double[][] P = null;
         a = mod.getA();
        P = mod.getP();
//        ## next call changes objects a, P if fast==TRUE
        KalmanForeRet x =  KalmanFore(nAhead, mod.getZ(), a, P,
                   mod.getT(), mod.getV(), mod.getH(), true);//, PACKAGE = "stats")
        return x;
    }
    private static KalmanForeRet KalmanFore(int nAhead, double[] z, double[] a,
			double[][] p2, double[][] t, double[][] v2, double h, boolean fast) {
    	double[]p0 = new double[p2.length * p2[0].length];
    	for(int i = 0; i < p2.length; i++){
    		for(int j = 0; j < p2[0].length; j++){
    			p0[i + j*p2.length]= p2[i][j];
    		}
    	}
    	double[]sT = new double[t.length * t[0].length];
    	for(int i = 0; i < t.length; i++){
    		for(int j = 0; j < t[0].length; j++){
    			sT[i + j*t.length]= t[i][j];
    		}
    	}
    	double[]sV = new double[v2.length * v2[0].length];
    	for(int i = 0; i < v2.length; i++){
    		for(int j = 0; j < v2[0].length; j++){
    			sV[i + j*v2.length]= v2[i][j];
    		}
    	}
    	double[] aCopy = new double[a.length];
    	System.arraycopy(a,0,aCopy,0,a.length);
    	return KalmanFore(nAhead, z, aCopy, p0, sT, sV,
 	            h,  fast);
	}

	static KalmanForeRet
	KalmanFore(int nahead, double[] sZ, double[] sa0, double[] sP0, double[] sT, double[] sV,
	           double sh, boolean fast)
	{
		KalmanForeRet res = new KalmanForeRet(); double[]forecasts; double[]se;
	    int  n = (nahead), p = (sa0.length);
	    double []Z = (sZ); double[]a = (sa0);double[]P = (sP0); double[]T = (sT);
	        double[]V = (sV); double h = (sh);
	    int i, j, k, l;
	    double fc, tmp; double[]mm; double[]anew;double[]Pnew;
	 
	    anew = new double[p];
	    Pnew = new double[p*p];
	    mm = new double[p*p];
	    forecasts = new double[n];
	    se = new double[n];
	    if (!(fast)){
	        a=(sa0);
	        P=(sP0);
	    }
	    for (l = 0; l < n; l++) {
	        fc = 0.0;
	        for (i = 0; i < p; i++) {
	            tmp = 0.0;
	            for (k = 0; k < p; k++)
	                tmp += T[i + p * k] * a[k];
	            anew[i] = tmp;
	            fc += tmp * Z[i];
	        }
	        for (i = 0; i < p; i++)
	            a[i] = anew[i];
	        (forecasts)[l] = fc;
	 
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                tmp = 0.0;
	                for (k = 0; k < p; k++)
	                    tmp += T[i + p * k] * P[k + p * j];
	                mm[i + p * j] = tmp;
	            }
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                tmp = V[i + p * j];
	                for (k = 0; k < p; k++)
	                    tmp += mm[i + p * k] * T[j + p * k];
	                Pnew[i + p * j] = tmp;
	            }
	        tmp = h;
	        for (i = 0; i < p; i++)
	            for (j = 0; j < p; j++) {
	                P[i + j * p] = Pnew[i + j * p];
	                tmp += Z[i] * Z[j] * P[i + j * p];
	            }
	        (se)[l] = tmp;
	    }
	    res.setForecasts(forecasts);
	    res.setSe(se);
	    return res;
	}

	static TransParsRet ARIMA_transPars(double[] sin, int[] sarma, boolean strans)
	{
		TransParsRet res = new TransParsRet();
		int []arma = (sarma); boolean trans = (strans);
	    int mp = arma[0], mq = arma[1], msp = arma[2], msq = arma[3],
	        ns = arma[4], i, j, p = mp + ns * msp, q = mq + ns * msq, v;
	    double []in = (sin); double[]params = (sin); double[]phi;double []theta;
	    res.setsPhi(new double [ p]);
	    res.setsTheta(new double[q]);
	    phi = res.getsPhi();
	    theta = res.getsTheta();
	    if (trans) {
	        int n = mp + mq + msp + msq;

	        params = new double[n];
	        for (i = 0; i < n; i++) params[i] = in[i];
	        if (mp > 0) partrans(mp, in, params);
	        v = mp + mq;
	        double[] in_v = new double[in.length - v];//?????
	        double[] params_v = new double[params.length - v];
	        if (msp > 0) partrans(msp, in_v, params_v);
	    }
	    if (ns > 0) {
	        /* expand out seasonal ARMA models */
	        for (i = 0; i < mp; i++) phi[i] = params[i];
	        for (i = 0; i < mq; i++) theta[i] = params[i + mp];
	        for (i = mp; i < p; i++) phi[i] = 0.0;
	        for (i = mq; i < q; i++) theta[i] = 0.0;
	        for (j = 0; j < msp; j++) {
	            phi[(j + 1) * ns - 1] += params[j + mp + mq];
	            for (i = 0; i < mp; i++)
	                phi[(j + 1) * ns + i] -= params[i] * params[j + mp + mq];
	        }
	        for (j = 0; j < msq; j++) {
	            theta[(j + 1) * ns - 1] += params[j + mp + mq + msp];
	            for (i = 0; i < mq; i++)
	                theta[(j + 1) * ns + i] += params[i + mp] *
	                    params[j + mp + mq + msp];
	        }
	    } else {
	        for (i = 0; i < mp; i++) phi[i] = params[i];
	        for (i = 0; i < mq; i++) theta[i] = params[i + mp];
	    }
	    return res;
	}

	static void partrans(int p, double []raw, double []newdata)
	{
	    int j, k;
	    double a;
	    int n = 0;
	    if (raw.length > newdata.length){
	    	n = raw.length;
	    }else{
	    	n = newdata.length;
	    }
	    double work[] = new double[n];

//	    if(p > 100) error(_("can only transform 100 pars in arima0"));
	    /* Step one: map (-Inf, Inf) to (-1, 1) via tanh
	       The parameters are now the pacf phi_{kk} */
	    for(j = 0; j < p; j++) work[j] = newdata[j] = Math.tanh(raw[j]);
	    /* Step two: run the Durbin-Levinson recursions to find phi_{j.},
	       j = 2, ..., p and phi_{p.} are the autoregression coefficients */
	    for(j = 1; j < p; j++) {
	        a = newdata[j];
	        for(k = 0; k < j; k++)
	            work[k] -= a * newdata[j - k - 1];
	        for(k = 0; k < j; k++) newdata[k] = work[k];
	    }
	}

	double[] TSconv(double[] a, double[] b)
	{   
	    int i, j, na, nb, nab;
	    double[] ab;
	    double []ra; double[]rb; double[]rab;

	    na = a.length;
	    nb = b.length;
	    nab = na + nb - 1;
	    ab = new double[nab];
	    ra = (a); rb = (b); rab = (ab);
	    for (i = 0; i < nab; i++) rab[i] = 0.0;
	    for (i = 0; i < na; i++)
	        for (j = 0; j < nb; j++)
	            rab[i + j] += ra[i] * rb[j];
	    return (ab);
	}

	/* based on code from AS154 */
	static void
	inclu2(int np, double []xnext, double []xrow, double ynext,
	       double []d, double []rbar, double []thetab)
	{
	    double cbar, sbar, di, xi, xk, rbthis, dpi;
	    int i, k, ithisr;

	/*   This subroutine updates d, rbar, thetab by the inclusion
	     of xnext and ynext. */

	    for (i = 0; i < np; i++) xrow[i] = xnext[i];

	    for (ithisr = 0, i = 0; i < np; i++) {
		if (xrow[i] != 0.0) {
		    xi = xrow[i];
		    di = d[i];
		    dpi = di + xi * xi;
		    d[i] = dpi;
		    cbar = di / dpi;
		    sbar = xi / dpi;
		    for (k = i + 1; k < np; k++) {
			xk = xrow[k];
			rbthis = rbar[ithisr];
			xrow[k] = xk - xi * rbthis;
			rbar[ithisr++] = cbar * rbthis + sbar * xk;
		    }
		    xk = ynext;
		    ynext = xk - xi * thetab[i];
		    thetab[i] = cbar * thetab[i] + sbar * xk;
		    if (di == 0.0) return;
		} else
		    ithisr = ithisr + np - i - 1;
	    }
	}

	double[][] getQ0(double[] sPhi, double[] sTheta)
	{
	    double[][] res;
	    int  p = (sPhi.length), q = (sTheta.length);
	    double []V; double []phi = (sPhi); double[]theta = (sTheta);

	    double []P; double[]xnext; double[] xrow; double[]rbar; double[]thetab;
	    int r = Math.max(p, q + 1);
	    int np = r * (r + 1) / 2, nrbar = np * (np - 1) / 2;
	    int indi, indj, indn;
	    double phii, phij, ynext, bi, vi, vj;
	    int   i, j, ithisr, ind, npr, ind1, ind2, npr1, im, jm;

	    //	    if(r > 350) error(_("maximum supported lag is 350"));
	    thetab = new double[np];
	    xnext = new double[np];
	    xrow = new double[np];
	    rbar = new double[nrbar];
	    V = new double[np];

	    for (ind = 0, j = 0; j < r; j++) {
		vj = 0.0;
		if (j == 0) vj = 1.0; else if (j - 1 < q) vj = theta[j - 1];
		for (i = j; i < r; i++) {
		    vi = 0.0;
		    if (i == 0) vi = 1.0; else if (i - 1 < q) vi = theta[i - 1];
		    V[ind++] = vi * vj;
		}
	    }

	    res = new double[r][r];
	    P = new double[r*r];
	    if (r == 1) {
		P[0] = 1.0 / (1.0 - phi[0] * phi[0]);
		res[0][0] = P[0];
		return res;
	    }
	    if (p > 0) {
	/*      The set of equations s * vec(P0) = vec(v) is solved for
		vec(P0).  s is generated row by row in the array xnext.  The
		order of elements in P is changed, so as to bring more leading
		zeros into the rows of s. */
		for (i = 0; i < nrbar; i++) rbar[i] = 0.0;
		for (i = 0; i < np; i++) {
		    P[i] = 0.0;
		    thetab[i] = 0.0;
		    xnext[i] = 0.0;
		}
		ind = 0;
		ind1 = -1;
		npr = np - r;
		npr1 = npr + 1;
		indj = npr;
		ind2 = npr - 1;
		for (j = 0; j < r; j++) {
		    phij = (j < p) ? phi[j] : 0.0;
		    xnext[indj++] = 0.0;
		    indi = npr1 + j;
		    for (i = j; i < r; i++) {
			ynext = V[ind++];
			phii = (i < p) ? phi[i] : 0.0;
			if (j != r - 1) {
			    xnext[indj] = -phii;
			    if (i != r - 1) {
				xnext[indi] -= phij;
				xnext[++ind1] = -1.0;
			    }
			}
			xnext[npr] = -phii * phij;
			if (++ind2 >= np) ind2 = 0;
			xnext[ind2] += 1.0;
			inclu2(np, xnext, xrow, ynext, P, rbar, thetab);
			xnext[ind2] = 0.0;
			if (i != r - 1) {
			    xnext[indi++] = 0.0;
			    xnext[ind1] = 0.0;
			}
		    }
		}

		ithisr = nrbar - 1;
		im = np - 1;
		for (i = 0; i < np; i++) {
		    bi = thetab[im];
		    for (jm = np - 1, j = 0; j < i; j++)
			bi -= rbar[ithisr--] * P[jm--];
		    P[im--] = bi;
		}

	/*        now re-order p. */

		ind = npr;
		for (i = 0; i < r; i++) xnext[i] = P[ind++];
		ind = np - 1;
		ind1 = npr - 1;
		for (i = 0; i < npr; i++) P[ind--] = P[ind1--];
		for (i = 0; i < r; i++) P[i] = xnext[i];
	    } else {

	/* P0 is obtained by backsubstitution for a moving average process. */

		indn = np;
		ind = np;
		for (i = 0; i < r; i++)
		    for (j = 0; j <= i; j++) {
			--ind;
			P[ind] = V[ind];
			if (j != 0) P[ind] += P[--indn];
		    }
	    }
	    /* now unpack to a full matrix */
	    for (i = r - 1, ind = np; i > 0; i--)
		for (j = r - 1; j >= i; j--)
		    P[r * i + j] = P[--ind];
	    for (i = 0; i < r - 1; i++)
		for (j = i + 1; j < r; j++)
		    P[i + r * j] = P[j + r * i];
	    for(i = 0; i < r; i++){
	    	for(j = 0; j < r; j++){
	    		res[i][j] = P[i*r+j];
	    	}
	    }
	    return res;
	}


	MakeARIMARet makeARIMA (double[] coefs, double[] phi, double[] theta, double[]Delta, double kappa)
	{
	    int p = (phi.length); int q = (theta.length);
	    int r = Math.max(p, q + 1); int d = (Delta.length);
	    int rd = r + d;
	    double [] Z = new double[r+Delta.length];
	    Z[0] = 1.0;
	    for(int i = 0; i < r-1; i++){
	    	Z[i + 1] = 0.0;
	    }
	    for(int i = 0; i < Delta.length; i++){
	    	Z[i + r] = Delta[i];
	    }

	    double[][]T = new double[rd][rd];
	    if(p > 0){
	    	for(int i = 0; i < p; i++){
	    		T[i][0] = phi[i];
	    }
	    }
	    if(r > 1) {
	    	for(int ind = 1; ind < r; ind++){
	    		T[ind-1][ind]= 1;
	    	}
	    }
	    if(d > 0) {
	        T[r] = Z;
	        if(d > 1) {
	            for(int ind = r+1; ind < r+d; ind++){
	            	T[ind][ind-1]= 1;
	            }
	        }
	    }
	    if(q < r - 1){
	    	double []thetaBack = theta;
	    	theta = new double [thetaBack.length + r - 1 -q];
	    	for(int i = 0; i < thetaBack.length; i++){
	    		theta[i] = thetaBack[i];
	    	}
	    	for(int i = 0; i < r-1-q; i++){
	    		theta[i+thetaBack.length] = 0;
	    	}
	    }
	    double []R = new double[1 + theta.length + d];
	    R[0] = 1;
	    System.arraycopy(theta, 0, R, 1, theta.length);
	    for(int i = 0; i < d; i++){
	    	int begin = 1;
	    	if (theta != null && theta.length != 0){
	    		begin += theta.length;
	    	}
	    	R[i+begin] = 0;
	    }
	    double[][]V = new double[R.length][R.length];
	    for(int i = 0; i < R.length; i++){
	    	for(int j = 0; j < R.length; j++){
	    		V[i][j] = R[i]*R[j];
	    	}
	    }
	    double h = 0.;
	    double[] a = new double[rd];
	    double[][]Pn = new double[rd][rd];
	    double[][]P = new double[rd][rd];
	    if(r > 1){ 
	    	double[][] q0= 	getQ0(phi, theta);
	    	for(int i = 0; i < r; i++){
	    		for(int j = 0; j < r; j++){
	    			Pn[i][j] = q0[i][j];
	    		}
	    	}
	    }
	    else{
	    	if(p > 0)
	    	{
	    		Pn[0][0] = 1/(1 - phi[0]*phi[0]);
	    	}else{
	    		Pn[0][0] = 1;
	    	}
	    }
	    if(d > 0) {
	    	for(int i = r; i < d + r; i++){
	    		for(int j = r; j < d + r; j++){
	    			Pn[i][j]= kappa;
	    		}
	    	}
	    }
	    MakeARIMARet res = new MakeARIMARet();
	    res.setPhi(phi);
	    res.setTheta(theta);
	    res.setDelta(Delta);
	    res.setZ(Z);
	    res.setA(a);
	    res.setP(P) ;
	    res.setT(T);
	    res.setV(V);
	    res.setH(h);
	    res.setPn(Pn);
	    res.setCoefs(coefs);
	    return res;

	    }

	LikeRet
	ARIMA_Like(double [] sy, double [] sPhi, double [] sTheta, double [] sDelta,
			double [] sa, double [] sP, double [] sPn, int sUP, boolean giveResid)
	{
		LikeRet res = new LikeRet();
	    double[] nres, sResid = null ;
	    int  n = (sy.length), rd = (sa.length), p = (sPhi.length),
	        q = (sTheta.length), d = (sDelta.length), r = rd - d;
	    double []y = (sy), a = (sa), P = (sP), Pnew = (sPn);
	    double [] phi = (sPhi), theta = (sTheta), delta = (sDelta);
	    double sumlog = 0.0, ssq = 0, resid, gain, tmp, vi;double []anew, mm = null,
	        M;         
	    int i, j, k, l, nu = 0; 
	    boolean useResid = (giveResid);
	    double []rsResid = null /* -Wall */;
	                
	    anew = new double[rd];
	    M = new double[rd];
	    if (d > 0) mm = new double[rd * rd];;
	                
	    if (useResid) { 
	        sResid = new double[n];
	        rsResid = (sResid);
	    }                   
	                        
	    for (l = 0; l < n; l++) {
	        for (i = 0; i < r; i++) {
	            tmp = (i < r - 1) ? a[i + 1] : 0.0;
	            if (i < p) tmp += phi[i] * a[0];
	            anew[i] = tmp; 
	        }               
	        if (d > 0) {
	            for (i = r + 1; i < rd; i++) anew[i] = a[i - 1];
	            tmp = a[0];
	            for (i = 0; i < d; i++) tmp += delta[i] * a[r + i];
	            anew[r] = tmp;
	        }       
	        if (l > (sUP)) {
	            if (d == 0) {
	                for (i = 0; i < r; i++) {
	                    vi = 0.0;
	                    if (i == 0) vi = 1.0; else if (i - 1 < q) vi = theta[i - 1];
	                    for (j = 0; j < r; j++) {
	                        tmp = 0.0;
	                        if (j == 0) tmp = vi; else if (j - 1 < q) tmp = vi * theta[j - 1];
	                        if (i < p && j < p) tmp += phi[i] * phi[j] * P[0];
	                        if (i < r - 1 && j < r - 1) tmp += P[i + 1 + r * (j + 1)];
	                        if (i < p && j < r - 1) tmp += phi[i] * P[j + 1];
	                        if (j < p && i < r - 1) tmp += phi[j] * P[i + 1];
	                        Pnew[i + r * j] = tmp;
	                    }
	                }
	            } else {
	                /* mm = TP */
	                for (i = 0; i < r; i++)
	                    for (j = 0; j < rd; j++) {
	                        tmp = 0.0;
	                        if (i < p) tmp += phi[i] * P[rd * j];
	                        if (i < r - 1) tmp += P[i + 1 + rd * j];
	                        mm[i + rd * j] = tmp;
	                    }
	                for (j = 0; j < rd; j++) {
	                    tmp = P[rd * j];
	                    for (k = 0; k < d; k++)
	                        tmp += delta[k] * P[r + k + rd * j];
	                    mm[r + rd * j] = tmp;
	                }
	                for (i = 1; i < d; i++)
	                    for (j = 0; j < rd; j++)
	                        mm[r + i + rd * j] = P[r + i - 1 + rd * j];

	                /* Pnew = mmT' */
	                for (i = 0; i < r; i++)
	                    for (j = 0; j < rd; j++) {
	                        tmp = 0.0;
	                        if (i < p) tmp += phi[i] * mm[j];
	                        if (i < r - 1) tmp += mm[rd * (i + 1) + j];
	                        Pnew[j + rd * i] = tmp;
	                    }
	                for (j = 0; j < rd; j++) {
	                    tmp = mm[j];
	                    for (k = 0; k < d; k++)
	                        tmp += delta[k] * mm[rd * (r + k) + j];
	                    Pnew[rd * r + j] = tmp;
	                }
	                for (i = 1; i < d; i++)
	                    for (j = 0; j < rd; j++)
	                        Pnew[rd * (r + i) + j] = mm[rd * (r + i - 1) + j];
	                /* Pnew <- Pnew + (1 theta) %o% (1 theta) */
	                for (i = 0; i <= q; i++) {
	                    vi = (i == 0) ? 1. : theta[i - 1];
	                    for (j = 0; j <= q; j++)
	                        Pnew[i + rd * j] += vi * ((j == 0) ? 1. : theta[j - 1]);
	                }
	            }
	        }
	        if (!Double.isNaN(y[l])) {
	            resid = y[l] - anew[0];
	            for (i = 0; i < d; i++)
	                resid -= delta[i] * anew[r + i];

	            for (i = 0; i < rd; i++) {
	                tmp = Pnew[i];
	                for (j = 0; j < d; j++)
	                    tmp += Pnew[i + (r + j) * rd] * delta[j];
	                M[i] = tmp;
	            }

	            gain = M[0];
	            for (j = 0; j < d; j++) gain += delta[j] * M[r + j];
	            if(gain < 1e4) {
	                nu++;
	                ssq += resid * resid / gain;
	                sumlog += Math.log(gain);
	            }
	            if (useResid) rsResid[l] = resid / Math.sqrt(gain);
	            for (i = 0; i < rd; i++)
	                a[i] = anew[i] + M[i] * resid / gain;
	            for (i = 0; i < rd; i++)
	                for (j = 0; j < rd; j++)
	                    P[i + j * rd] = Pnew[i + j * rd] - M[i] * M[j] / gain;
	        } else {
	            for (i = 0; i < rd; i++) a[i] = anew[i];
	            for (i = 0; i < rd * rd; i++) P[i] = Pnew[i];
	            if (useResid) rsResid[l] = Double.NaN;
	        }
	    }

	    if (useResid) {
	        nres = new double[3];
	        (nres)[0] = ssq;
	        (nres)[1] = sumlog;
	        (nres)[2] = (double) nu;
	        res.setNres(nres);
	        res.setsResid(sResid);
	        return res;
	    } else {
	        nres = new double[3];
	        (nres)[0] = ssq;
	        (nres)[1] = sumlog;
	        (nres)[2] = (double) nu;
	        res.setNres(nres);
	        return res;
	    }
	}
}
