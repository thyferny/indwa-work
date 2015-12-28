
package com.alpine.datamining.operator.svd;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class SVDLanczosPGGP  extends AbstractSVDLanczos {
    private static Logger itsLogger = Logger.getLogger(SVDLanczosPGGP.class);

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
		+initValue+")";
		return sql;
	}

	protected void dropUVTable()
			throws SQLException {
		String sql;
		if (para.getUdrop() != 0){
			sql = "Drop TABLE  if exists "+para.getUmatrix();
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
		if (para.getVdrop() != 0){
			sql = "Drop  TABLE if exists "+para.getVmatrix();
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
	}

	protected void dropSingularTable()
			throws SQLException {
		String sql;
		if (para.getSingularValueDrop() != 0){
			sql = "Drop  TABLE if exists "+para.getSingularValue();//+para.getVmatrix();
			itsLogger.debug("SVDLanczos.train():sql="+sql);
			st.execute(sql);
		}
	}
	protected void dropTempTable()throws SQLException{
	}

	protected void putBSVDIntoDB() throws SQLException {
		databaseConnection.getConnection().setAutoCommit(false);   
		st = databaseConnection.createStatement(false);
		String sqlCreateU = "create temp table "+tempUName+" (matrixrow int, matrixcol int, val float) "+sqlGenerator.setCreateTableEndingSql(null);
		itsLogger.debug(sqlCreateU);
		String sqlCreateV = "create temp table "+tempVName+" (matrixrow int, matrixcol int, val float) "+sqlGenerator.setCreateTableEndingSql(null);
		itsLogger.debug(sqlCreateV);
		String sqlSingularValues = "create table "+para.getSingularValue()+" (\"alpine_feature\" int, "+StringHandler.doubleQ(value)+" float) "+(sAppendOnly ? sqlGenerator.getStorageString(sStorageParameters.isAppendOnly(), sStorageParameters.isColumnarStorage(), sStorageParameters.isCompression(), sStorageParameters.getCompressionLevel()):" ")+sqlGenerator.setCreateTableEndingSql(sStorageParameters == null ? null : sStorageParameters.getSqlDistributeString());
		itsLogger.debug(sqlSingularValues);
		st.addBatch(sqlCreateU);
		st.addBatch(sqlCreateV);
		st.addBatch(sqlSingularValues);
		addBSVDBatch();
		st.executeBatch();	
		databaseConnection.getConnection().commit();
		databaseConnection.getConnection().setAutoCommit(true);
	}
}
