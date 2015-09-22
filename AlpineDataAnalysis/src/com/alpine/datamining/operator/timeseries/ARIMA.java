package com.alpine.datamining.operator.timeseries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

public class ARIMA extends Trainer {
    private static final Logger logger = Logger.getLogger(ARIMA.class);

    private ARIMARParameter para;
	private String idColumn;
	private String valueColumn;

	private int count = 0;
	private int n = 0;
	private int p = 0, q = 0, d = 0;
//	private double [] data;
	private double[] z;
	private double[] w;
	private double mu;
	private double[] u;
	private double [] v;

	private double[] phi;
	private double[] theta;
	private double[] bestPhi;
	private double[] bestTheta;

	private double[] deltaPhi;
	private double[] deltaTheta;
	private double error = 0.00001;
	private int maxCycleDefault = 10;
	private Matrix X = null;
	private Matrix Y = null;

	public ARIMA() {
		super();
	}

	@Override
	public Model train(DataSet dataSet) throws OperatorException {
		getData(dataSet);
		int maxCycle = maxCycleDefault;
		p = para.getP();
		q = para.getQ();
		d = para.getD();
		X = new Matrix(n,p+q);
//		a0 = new double[n];
		w = new double[n];
		u = new double[n];
		v = new double[n];
		phi = new double[p];
		theta = new double[q];
		bestPhi = new double[p];
		bestTheta = new double[q];
		deltaPhi = new double[p];
		deltaTheta = new double[q];

		for(int i = 0; i < n; i++){
			w[i] = 0;
			u[i] = 0;
			v[i] = 0;
		}
		for(int i = 0; i < p; i++){
			phi[i] = 0;
			bestPhi[i] = 0;
			deltaPhi[i] = 0;
		}
		for(int i = 0; i < q; i++){
			theta[i] = 0;
			bestTheta[i] = 0;
			deltaTheta[i] = 0;
		}

		Y = new Matrix(n,1);
		for(int j = 0; j < n; j++){
			w[j] = z[j];
		}		
		for (int i = 0; i < d; i++){
			//for(int j = i + 1; j < n; j++){
			for(int j = n - 1; j > i; j--){
				w[j] = w[j] - w[j - 1];
			}
			for(int j = 0; j <= i; j++){
//				w[j] = Double.NaN;
				w[j] = 0.0;
			}
		}
		for(int i = 0; i < w.length; i++){
			if (!Double.isNaN(w[i]) && i >= d){
				mu += w[i]/w.length;
			}
		}
		for(int i = 0; i < w.length; i++){
			if (!Double.isNaN(w[i]) && i >= d){
				w[i] -= mu;
			}
		}
//		offset = d;
		double bestss = Double.MAX_VALUE;
		//init theta, phi;
//		for(int i = 0; i < p; i++){
//			phi[i] = 0.8/p;
////			phi[i] = 0.1;
//		}
//		for(int i = 0; i< q; i++){
//			theta[i] = 0.8/q;
////			theta[i] = 0.1;
//		}
		for(int cycle = 0; cycle < maxCycle; cycle++){
			// init
			logger.debug("cycle:"+cycle+";1");
			double ss = 0;
			String Ystr = "";
			String ustr = "";
			String vstr = "";
			for(int i = 0; i < n; i++){
				Y.set(i, 0, fw(i));
				for(int j = 0; j < p; j++){
					Y.set(i, 0, Y.get(i, 0) - phi[j] * fw(i - j - 1));
				}
				for(int k = 0; k < q; k++){
					Y.set(i, 0, Y.get(i, 0) + theta[k] * fy(i - k - 1));
				}
				ss += Y.get(i, 0) * Y.get(i, 0);
				u[i] = fy(i);
				for (int j= 0; j < p ;j++){
					u[i] += phi[j] * fu(i - j - 1);
				}
				v[i] = -1 * fy(i);
				for(int j = 0; j < q; j++){
					v[i] += theta[j]*fv(i - j - 1);
				}
				Ystr += ","+fy(i);
				ustr += ","+fy(i);
				vstr += ","+fv(i);
			}
			
			if (ss < bestss){
				System.arraycopy(phi, 0, bestPhi, 0, phi.length); 
				System.arraycopy(theta, 0, bestTheta, 0, theta.length); 
				bestss = ss;
			}
			logger.debug("cycle:"+cycle+";2;Ystr:"+Ystr);
			logger.debug("cycle:"+cycle+";2;ustr:"+ustr);
			logger.debug("cycle:"+cycle+";2;vstr:"+vstr);

			for(int i = 0; i < n; i++){
				for(int j = 0; j < p; j++){
					X.set(i, j, fu(i - j - 1));
				}
				for(int j = 0; j < q; j++){
					X.set(i, j + p, fv(i - j - 1));
				}
			}
			logger.debug("cycle:"+cycle+";3");

			Matrix B = null;
//			if (cycle == 3){
//				B = X.transpose().times(X).inverse(true).times(X.transpose()).times(Y);
//			}else{
			try{
				B = X.transpose().times(X).SVDInverse().times(X.transpose()).times(Y);

			}catch(OperatorException e){
				logger.debug(e.getLocalizedMessage());
				break;
			}

//			}

			double delta = 0;
			logger.debug("cycle:"+cycle+";4");

			String tpStr = "";
			tpStr += "Phi:";
			String deltaStr = "";
			deltaStr+="deltaPhi:";
			for (int i = 0; i < p; i++){
				deltaPhi[i] = B.get(i, 0);
				deltaStr+=","+deltaPhi[i];
				delta += deltaPhi[i] * deltaPhi[i];
				phi[i] += deltaPhi[i];
				tpStr+=","+phi[i];
			}
			logger.debug("cycle:"+cycle+";5");

			tpStr += ";Theta:";
			deltaStr+=";deltaTheta:";
			for(int i = 0; i < q; i++){
				deltaTheta[i] = B.get(i + p, 0);
				deltaStr+=","+deltaTheta[i];
				delta += deltaTheta[i] * deltaTheta[i];
				theta[i] += deltaTheta[i];
				tpStr+=","+theta[i];
			}

			logger.debug("cycle:"+cycle+";ss:"+ss+";delta:"+delta+";thetaphi:"+tpStr+";delta:"+deltaStr);

			if (delta < error){
				logger.debug("break");
				break;
			}
		}
		return new ARIMAModel(dataSet ,idColumn, valueColumn, p, d, q, bestPhi, bestTheta, Double.NaN,null, w, null, mu,null,null,0,0,0);
	}
	private double fw(int i){
		if (i < d || i >= n){
			return 0;
		}else{
			return w[i];
		}
	}

	private double fy(int i){
		if (i < d || i >= n){
			return 0;
		}else{
			return Y.get(i, 0);
		}
	}
	
	private double fu(int i){
		if (i < d || i >= n){
			return 0;
		}else{
			return u[i];
		}		
	}

	private double fv(int i){
		if (i < d || i >= n){
			return 0;
		}else{
			return v[i];
		}
	}	
	
	private double[] getData(DataSet dataSet) throws OperatorException{
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		
		String tableName=((DBTable) dataSet.getDBTable()).getTableName();
		Statement st = null;

		String idColumn = StringHandler.doubleQ(para.getIdColumn());
		String valueColumn = StringHandler.doubleQ(para.getValueColumn());

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		long dataCount = 0;
		int threshold = para.getThreshold();
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("select count(*) from ").append(tableName);
			logger.debug("ARIMA.getData():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());			
			if(rs.next()) {
				dataCount = rs.getLong(1);
				if (dataCount > threshold){
					z = new double[threshold];
				}else{
//		    	data = new double[dataCount];
					z = new double[(int)dataCount];
				}
			}
			sql = new StringBuffer();
			sql.append("select ").append(valueColumn).append(" from ").append(tableName).append(" order by ").append(idColumn);
			if( dataCount > threshold){
				sql.append(" offset  ").append(dataCount - threshold).append(" limit ").append(threshold);
			}
			logger.debug("ARIMA.getData():sql="+sql);
			rs = st.executeQuery(sql.toString());
			int i = 0;
			while (rs.next()) {
		    	double value = rs.getDouble(1);
//		    	data[i] = value;
		    	z[i] = value;
//		    	itsLogger.debug("data["+i+"]:"+data[i]);
		    	i++;
			}
			count = z.length;
			n = count;
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
//		z = data;
		return z;
	}

}
