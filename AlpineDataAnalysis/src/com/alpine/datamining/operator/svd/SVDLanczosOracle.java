
package com.alpine.datamining.operator.svd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.tools.matrix.SingularValueDecomposition;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class SVDLanczosOracle  extends AbstractSVDLanczos {
    private static final Logger itsLogger = Logger.getLogger(SVDLanczosOracle.class);
    protected String getSVDSQL() {
		String sql;
		sql = " select alpine_miner_svd_l('"
		+tableName+"','"
		+tempPName+"','"
		+tempQName+"','"
		+mName+"','"
		+nName+"','"
		+StringHandler.doubleQ(value)+"',"
		+para.getNumFeatures()+","
		+initValue+") from dual";
		return sql;
	}

	protected void createUVTable() throws SQLException {
		String sqlU = "create table " + matrixUName
				+(uAppendOnly ? sqlGenerator.getStorageString(uStorageParameters.isAppendOnly(), uStorageParameters.isColumnarStorage(), uStorageParameters.isCompression(), uStorageParameters.getCompressionLevel()):" ")
				+ " as select " + tempPName
				+ ".m_column as " + mName + ", " + tempUName
				+ ".matrixcol as \"alpine_feature\" , sum(cast(" + tempPName
				+ ".val as binary_double) * " + tempUName + ".val) as "
				+ StringHandler.doubleQ(value) + " from " + tempPName
				+ " join " + tempUName + " on " + tempPName + ".n_column = "
				+ tempUName + ".matrixrow group by " + tempPName
				+ ".m_column, " + tempUName + ".matrixcol "
				+ sqlGenerator.setCreateTableEndingSql(uStorageParameters == null ? null : uStorageParameters.getSqlDistributeString());
		itsLogger.debug("SVDLanczos.train():sql=" + sqlU);
		st.execute(sqlU);
		String sqlV = "create table " + matrixVName 
				+(vAppendOnly ? sqlGenerator.getStorageString(vStorageParameters.isAppendOnly(), vStorageParameters.isColumnarStorage(), vStorageParameters.isCompression(), vStorageParameters.getCompressionLevel()):" ") 
				+ " as select " + tempQName
				+ ".n_column as " + nName + " , " + tempVName
				+ ".matrixcol as \"alpine_feature\" , sum(cast(" + tempQName
				+ ".val as binary_double)* " + tempVName + ".val) as "
				+ StringHandler.doubleQ(value) + " from " + tempQName
				+ " join " + tempVName + " on " + tempQName + ".m_column = "
				+ tempVName + ".matrixrow group by " + tempQName
				+ ".n_column, " + tempVName + ".matrixcol "
				+ sqlGenerator.setCreateTableEndingSql(vStorageParameters == null ? null : vStorageParameters.getSqlDistributeString());
		itsLogger.debug("SVDLanczos.train():sql=" + sqlV);
		st.execute(sqlV);
	}

	protected void dropUVTable()
			throws SQLException {
		String sql;
		if (para.getUdrop() != 0){
			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+para.getUmatrix()+"')";
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
		if (para.getVdrop() != 0){
			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+para.getVmatrix()+"')";
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
	}

	protected void dropSingularTable()
			throws SQLException {
		String sql;
		if (para.getSingularValueDrop() != 0){
			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+para.getSingularValue()+"')";//+para.getVmatrix();
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
		sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempUName+"')";//+para.getVmatrix();
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);

		sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempVName+"')";
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);
	}


	protected void putBSVDIntoDB() throws SQLException {
		databaseConnection.getConnection().setAutoCommit(false);   
		st = databaseConnection.createStatement(false);
		String sqlCreateU = "create table "+tempUName+" (matrixrow int, matrixcol int, val binary_double)";
		String sqlCreateV = "create table "+tempVName+" (matrixrow int, matrixcol int, val binary_double)";
		String sqlSingularValues = "create table "+para.getSingularValue()+" (\"alpine_feature\" int, "+StringHandler.doubleQ(value)+" binary_double) "
		+(sAppendOnly ? sqlGenerator.getStorageString(sStorageParameters.isAppendOnly(), sStorageParameters.isColumnarStorage(), sStorageParameters.isCompression(), sStorageParameters.getCompressionLevel()):" ")+sqlGenerator.setCreateTableEndingSql(sStorageParameters == null ? null : sStorageParameters.getSqlDistributeString());
		st.addBatch(sqlCreateU);
		st.addBatch(sqlCreateV);
		st.addBatch(sqlSingularValues);
		addBSVDBatch();
		st.executeBatch();	
		databaseConnection.getConnection().commit();
		databaseConnection.getConnection().setAutoCommit(true);
	}
	protected void dropTempTable()throws SQLException{
		String sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempPName+"')";
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempQName+"')";
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempUName+"')";//+para.getVmatrix();
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+tempVName+"')";
		itsLogger.debug("SVDLanczos.train():sql="+sql);
		st.execute(sql);
	}

	protected void trainGetBSVD(String sql) throws SQLException,
			OperatorException {
		ResultSet rs;
		itsLogger.debug("SVDLanczosOracle.train():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		ArrayList<Double> result = new ArrayList<Double>();
		if (rs.next()) {
			ResultSet resultSet =  rs.getArray(1).getResultSet();
			while(resultSet.next()){
				result.add(resultSet.getInt(1) - 1,resultSet.getDouble(2));
			}
		}
		if (result.size() % 2 != 0) {
			itsLogger.error(
					"SVDLanczosOracle.train():result length Wrong: " + result.size());
			ArrayList<Double> resultNew = new ArrayList<Double>();;
			for (int i = 0; i < result.size() - 1; i++) {
				resultNew.add(result.get(i));
			}
			result = resultNew;
		}
		double[][] B = new double[result.size() / 2][result.size() / 2];
		for (int i = 0; i < result.size() / 2; i++) {
				double alpha = result.get(i).doubleValue();
				double beta = result.get(i + result.size() / 2)
						.doubleValue();
				B[i][i] = alpha;
				if (i != result.size() / 2 - 1) {
					B[i][i + 1] = beta;
				}
		}
		SingularValueDecomposition svd = new SingularValueDecomposition(
				new Matrix(B));
		U = svd.getU();
		V = svd.getV();
		singularValues = svd.getSingularValues();
	}

}
