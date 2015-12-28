
package com.alpine.datamining.operator.svm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class SVMNoveltyDetectionLearnerOracle extends AbstractSVMLearner {
    private static final Logger itsLogger = Logger.getLogger(SVMNoveltyDetectionLearnerOracle.class);
    public Model train(DataSet dataSet, SVMParameter parameter) throws OperatorException {
		para = (SVMParameter) parameter;
		setDataSourceInfo(DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName()));
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		DataSet newDataSet = getTransformer()
		.TransformCategoryToNumeric_new(dataSet);
		String newTableName = ((DBTable) newDataSet
				.getDBTable()).getTableName();

		Statement st = null;
		ResultSet rs = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer ind = getColumnArray(newDataSet);
		StringBuffer where = getColumnWhere(newDataSet);
		SVMNoveltyDetectionModel model = new SVMNoveltyDetectionModel(dataSet, newDataSet);
		if(!newDataSet.equals(dataSet))
		{
			model.setAllTransformMap_valueKey(getTransformer().getAllTransformMap_valueKey());
		}
		model.setKernelType(para.getKernelType());
		model.setDegree(para.getDegree());
		model.setGamma(para.getGamma());
		String sql = "select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights, (model).individuals from (select alpine_miner_online_sv_nd('"+newTableName+"','"+ind+"',"+where+","+para.getKernelType()+","+para.getDegree()+","+para.getGamma()+","+para.getEta()+","+para.getNu()+") as model";
		sql += " from dual ";
		sql += ") a";
		try{
			itsLogger.debug("SVMNoveltyDetectionLearnerOracle.train():sql="+sql);
			rs = st.executeQuery(sql);
			setModel(rs, model);
			if(getTransformer().isTransform())
			{
				dropTable(st, newTableName);
			}
			rs.close();
			st.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return model;
	}
	public StringBuffer getColumnWhere(DataSet newDataSet){
		StringBuffer where = new StringBuffer();
		where.append("varchar2array(");
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		boolean first = true;
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.escQ(StringHandler.doubleQ(att.getName()));
			if(!first){
				where.append(",");
			}else{
				first = false;
			}
			where.append("'").append(columnName).append("'");
		}
		where.append(")");
		return where;
	}
	public void setModel(ResultSet rs, SVMModel model) throws SQLException{
		try {
			if(rs.next()){
				model.setInds(rs.getInt(1));
				model.setCumErr(rs.getDouble(2));
				model.setEpsilon(rs.getDouble(3));
				model.setRho(rs.getDouble(4));
				model.setB(rs.getDouble(5));
				model.setNsvs(rs.getInt(6));
				model.setIndDim(rs.getInt(7));
				Double [] weights = null;
				ArrayList<Double> results = new ArrayList<Double>();
				ResultSet resultSet = rs.getArray(8).getResultSet();
				if(resultSet != null){
					while(resultSet.next()){
						results.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
					}
				}

//					BigDecimal[] results = (BigDecimal[])rs.getArray(8).getArray();
					if (results != null){
						weights = new Double[model.getNsvs()];
						for(int i = 0; i < results.size() && i < model.getNsvs(); i++){
							weights[i] = results.get(i).doubleValue();
						}
					}
				Double[] weightsArray = null; 
				if (weights != null ){
					weightsArray = new Double[model.getNsvs()];
					for(int i = 0; i < weights.length && i < model.getNsvs(); i++){
						if (weights[i] != null && !Double.isNaN(weights[i])){
							weightsArray[i] = weights[i].doubleValue();
						}else{
							weightsArray[i] = 0.0;
						}
					}
				}
				model.setWeights(weightsArray);
				Double [] individuals = null;
				resultSet = rs.getArray(9).getResultSet();
				results = new ArrayList<Double>();
				if(resultSet != null){
					while(resultSet.next()){
						results.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
					}
				}
					if (results != null){
						individuals = new Double[model.getNsvs() * model.getIndDim()];
						for(int i = 0; i < results.size() && i < model.getNsvs() * model.getIndDim(); i++){
							if (results.get(i) != null){
								individuals[i] = results.get(i).doubleValue();
							}
						}
					}
				Double[] individualsArray = null;
				if(individuals != null){
					individualsArray = new Double[model.getNsvs() * model.getIndDim()];
					for(int i = 0; i < individuals.length && i < model.getNsvs() * model.getIndDim(); i++){
								 if (individuals[i] != null && !Double.isNaN(individuals[i])){
									 individualsArray[i] = individuals[i].doubleValue();
								 }else{
									 individualsArray[i] = 0.0;
								 }
					}
				}else{
					individualsArray = null;
				}
				model.setIndividuals(individualsArray);
			}
		} catch (SQLException e) {
			throw e;
		}
	}

}
