
package com.alpine.datamining.operator.svd;

import java.sql.SQLException;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.tools.matrix.SingularValueDecomposition;
import com.alpine.datamining.utility.TableTransferParameter;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class SVDLanczosNetezza  extends AbstractSVDLanczos {
    private static final Logger itsLogger = Logger.getLogger(SVDLanczosNetezza.class);

    String alphaBetaTable = "R"+System.currentTimeMillis();

	protected String getSVDSQL() {
		String sql;
		sql = " call alpine_miner_svd_l('"
		+tableName+"','"
		+tempPName+"','"
		+tempQName+"','"
		+mName+"','"
		+nName+"','"
		+StringHandler.doubleQ(value)+"',"
		+para.getNumFeatures()+","
		+initValue+",'"
		+alphaBetaTable+"')";
		return sql;
	}

	protected void trainGetBSVD(String sql) throws SQLException,
			OperatorException {
		TableTransferParameter.createDoubleTable(alphaBetaTable, st);
		itsLogger.debug("SVDLanczosNetezza.train():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		Double[] result = TableTransferParameter.getDoubleResult(alphaBetaTable, st);
		if (result.length % 2 != 0) {
			itsLogger.error(
					"SVDLanczosNetezza.train():result length Wrong: " + result.length);
			Double[] resultNew = new Double[result.length - 1];
			for (int i = 0; i < result.length - 1; i++) {
				resultNew[i] = result[i];
			}
			result = resultNew;
		}
		double[][] B = new double[result.length / 2][result.length / 2];
		for (int i = 0; i < result.length / 2; i++) {
			if (result[i] instanceof Number) {
				double alpha = ((Number) result[i]).doubleValue();
				double beta = ((Number) result[i + result.length / 2])
						.doubleValue();
				B[i][i] = alpha;
				if (i != result.length / 2 - 1) {
					B[i][i + 1] = beta;
				}
			}
		}
		TableTransferParameter.dropResultTable(alphaBetaTable, st);

		SingularValueDecomposition svd = new SingularValueDecomposition(
				new Matrix(B));
		U = svd.getU();
		V = svd.getV();
		singularValues = svd.getSingularValues();
	}

	protected void dropUVTable()
			throws SQLException {
		String sql;
		if (para.getUdrop() != 0){
			sql = "call droptable_if_existsdoubleq('"+para.getUmatrix().split("\\.",2)[1]+"')";
			itsLogger.debug("SVDLanczosNetezza.train():sql="+sql);
			st.execute(sql);
		}
		if (para.getVdrop() != 0){
			sql = "call droptable_if_existsdoubleq('"+para.getVmatrix().split("\\.",2)[1]+"')";
			itsLogger.debug("SVDLanczosNetezza.train():sql="+sql);
			st.execute(sql);
		}
	}

	protected void dropSingularTable()
			throws SQLException {
		String sql;
		if (para.getSingularValueDrop() != 0){
			sql = "call droptable_if_existsdoubleq('"+para.getSingularValue().split("\\.",2)[1]+"')";
			itsLogger.debug("SVDLanczosNetezza.train():sql="+sql);
			st.execute(sql);
		}
	}
	protected void dropTempTable()throws SQLException{
	}

	protected void putBSVDIntoDB() throws SQLException {
		databaseConnection.getConnection().setAutoCommit(false);   
		st = databaseConnection.createStatement(false);
		String sqlCreateU = "create temp table "+tempUName+" (matrixrow int, matrixcol int, val float) "+sqlGenerator.setCreateTableEndingSql(null);
		itsLogger.debug("SVDLanczosNetezza.putBSVDIntoDB()"+sqlCreateU);
		String sqlCreateV = "create temp table "+tempVName+" (matrixrow int, matrixcol int, val float) "+sqlGenerator.setCreateTableEndingSql(null);
		itsLogger.debug("SVDLanczosNetezza.putBSVDIntoDB()"+sqlCreateV);
		String sqlSingularValues = "create table "+para.getSingularValue()+" (\"alpine_feature\" int, "+StringHandler.doubleQ(value)+" float) "+(sAppendOnly ? sqlGenerator.getStorageString(sStorageParameters.isAppendOnly(), sStorageParameters.isColumnarStorage(), sStorageParameters.isCompression(), sStorageParameters.getCompressionLevel()):" ")+sqlGenerator.setCreateTableEndingSql(sStorageParameters == null ? null : sStorageParameters.getSqlDistributeString());
		itsLogger.debug("SVDLanczosNetezza.putBSVDIntoDB()"+sqlSingularValues);
		st.addBatch(sqlCreateU);
		st.addBatch(sqlCreateV);
		st.addBatch(sqlSingularValues);
		addBSVDBatch();
		st.executeBatch();	
		databaseConnection.getConnection().commit();
		databaseConnection.getConnection().setAutoCommit(true);
	}
}
