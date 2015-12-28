
package com.alpine.datamining.operator.svd;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.tools.matrix.SingularValueDecomposition;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class SVDLanczosDB2  extends AbstractSVDLanczos {
    private static final Logger itsLogger = Logger.getLogger(SVDLanczosDB2.class);
    public Model train(DataSet dataSet, SVDParameter para) throws OperatorException {
		this.para = para;
		uStorageParameters = para.getUmatrixTableStorageParameters();
		vStorageParameters = para.getVmatrixTableStorageParameters();
		sStorageParameters = para.getSingularValueTableStorageParameters();
		if(uStorageParameters == null || !uStorageParameters.isAppendOnly()){
			uAppendOnly = false;
		}else{
			uAppendOnly = true;
		}
		if(vStorageParameters == null || !vStorageParameters.isAppendOnly()){
			vAppendOnly = false;
		}else{
			vAppendOnly = true;
		}
		if(sStorageParameters == null || !sStorageParameters.isAppendOnly()){
			sAppendOnly = false;
		}else{
			sAppendOnly = true;
		}
		tableName = ((DBTable) dataSet.getDBTable())
		.getTableName();
		databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
        this.dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		String sql = "";
		try {
			st = databaseConnection.createStatement(false);
			calculateCount();
			determineMN();
			adjustFeatures();
			tempPName =  "P"+System.currentTimeMillis();
			tempQName =  "Q"+System.currentTimeMillis();
			tempUName =  "U"+System.currentTimeMillis();
			tempVName =  "V"+System.currentTimeMillis();

			value = dataSet.getColumns().getLabel().getName();
			sql = getSVDSQL();

			trainGetBSVD(sql);
			try {
				dropSingularTable();
			}catch(SQLException e){
				itsLogger.error(e.getMessage(),e);
			}
			st.close();
			putBSVDIntoDB();
			st.close();
			st = databaseConnection.createStatement(false);
			try {
				dropUVTable();
			}catch(SQLException e){
				itsLogger.error(e.getMessage(),e);
			}
			createUVTable();
			dropTempTable();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage ());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null){
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage ());
			}
		}
		return new SVDModel(dataSet,  para.getUmatrix(), para.getVmatrix() ,StringHandler.doubleQ(para.getColName()), StringHandler.doubleQ(para.getRowName()));
	}

	protected void createUVTable()
			throws SQLException {
		String sqlU = "create table "+matrixUName+" as (select "+tempPName+".m_column as "+mName+", "+tempUName+".matrixcol as \"alpine_feature\" , sum("+tempPName+".val * "+tempUName+".val) as "+StringHandler.doubleQ(value)+" from "+tempPName+" join "+tempUName+" on "+tempPName+".n_column = "+tempUName+".matrixrow group by "+tempPName+".m_column, "+tempUName+".matrixcol "+") definition only"; 
		itsLogger.debug("SVDLanczosDB2.train():sql="+sqlU);
		st.execute(sqlU);
		sqlU = "insert into "+matrixUName+"  select "+tempPName+".m_column as "+mName+", "+tempUName+".matrixcol as \"alpine_feature\" , sum("+tempPName+".val * "+tempUName+".val) as "+StringHandler.doubleQ(value)+" from "+tempPName+" join "+tempUName+" on "+tempPName+".n_column = "+tempUName+".matrixrow group by "+tempPName+".m_column, "+tempUName+".matrixcol "; 
		itsLogger.debug("SVDLanczosDB2.train():sql="+sqlU);
		st.execute(sqlU);
		String sqlV = "create table "+matrixVName+" as (select "+tempQName+".n_column as "+nName+" , "+tempVName+".matrixcol as \"alpine_feature\" , sum("+tempQName+".val * "+tempVName+".val) as "+StringHandler.doubleQ(value)+" from "+tempQName+" join "+tempVName+" on "+tempQName+".m_column = "+tempVName+".matrixrow group by "+tempQName+".n_column, "+tempVName+".matrixcol "+") definition only"; 
		itsLogger.debug("SVDLanczosDB2.train():sql="+sqlV);
		st.execute(sqlV);
		sqlV = "insert into "+matrixVName+"  select "+tempQName+".n_column as "+nName+" , "+tempVName+".matrixcol as \"alpine_feature\" , sum("+tempQName+".val * "+tempVName+".val) as "+StringHandler.doubleQ(value)+" from "+tempQName+" join "+tempVName+" on "+tempQName+".m_column = "+tempVName+".matrixrow group by "+tempQName+".n_column, "+tempVName+".matrixcol "; 
		itsLogger.debug("SVDLanczosDB2.train():sql="+sqlV);
		st.execute(sqlV);
	}
//	protected abstract void dropTempTable()throws SQLException;
//	protected abstract void putBSVDIntoDB() throws SQLException;

	protected void addBSVDBatch() throws SQLException {
		for ( int i = 0 ; i < U.getRowDimension(); i++){
			for( int j = 0; j  < U.getColumnDimension(); j++){
				String insertU = "insert into "+tempUName+" values("+(i + 1)+","+(j+1)+","+U.get(i, j)+")";
				st.addBatch(insertU);
			}
		}
		for ( int i = 0 ; i < V.getRowDimension(); i++){
			for( int j = 0; j  < V.getColumnDimension(); j++){
				String insertV = "insert into "+tempVName+" values("+(i+1)+","+(j+1)+","+V.get(i, j)+")";
				st.addBatch(insertV);
			}
		}
		for ( int i = 0 ; i < singularValues.length; i++){
			String insertSingularValues = "insert into "+para.getSingularValue()+" values("+(i+1)+","+singularValues[i]+")";
			st.addBatch(insertSingularValues);
		}
	}

	protected void trainGetBSVD(String sql)
			throws SQLException, OperatorException {
//		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
//		rs = st.executeQuery(sql.toString());
			itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); 
			stpCall.registerOutParameter(1, java.sql.Types.ARRAY);
			Object[] result = null;
			stpCall.execute();
			result = (Number[])stpCall.getArray(1).getArray();
			stpCall.close();
		if (result.length%2 != 0){
			itsLogger.error("SVDLanczosDB2.train():result length Wrong: "+result.length);
			Object[] resultNew = new Object[result.length - 1]; 
			for(int i = 0; i < result.length - 1; i++){
				resultNew[i] = result[i];
			}
			result = resultNew;
		}
		double[][]B = new double[result.length/2][result.length/2]; 
		for(int i = 0; i < result.length/2; i++){
			if(result[i] instanceof Number){
				double alpha = ((Number)result[i]).doubleValue();
				double beta = ((Number)result[i + result.length/2]).doubleValue();
				B[i][i] = alpha;
				if(i != result.length/2 - 1){
					B[i][i+1] = beta;
				}
			}
		}
		SingularValueDecomposition svd = new SingularValueDecomposition(new Matrix(B));
		U = svd.getU();
		V = svd.getV();
		singularValues = svd.getSingularValues();
	}

	private void adjustFeatures() {
		if(colCount < para.getNumFeatures() || rowCount < para.getNumFeatures()){
			para.setNumFeatures((int)(colCount>rowCount?rowCount:colCount));
		}
	}

	private void determineMN() {
		if (colCount > rowCount){
			initValue = 1.0/Math.sqrt(rowCount);
			mName = StringHandler.doubleQ(para.getColName());
			nName = StringHandler.doubleQ(para.getRowName());
			matrixUName = para.getVmatrix();
			matrixVName = para.getUmatrix();
			boolean tempAppendOnly =  uAppendOnly;
			uAppendOnly = vAppendOnly; 
			vAppendOnly = tempAppendOnly; 
			AnalysisStorageParameterModel tempStorageParameters = uStorageParameters;
			uStorageParameters = vStorageParameters;
			vStorageParameters = tempStorageParameters;
		}else{
			initValue = 1.0/Math.sqrt(colCount);
			mName = StringHandler.doubleQ(para.getRowName());
			nName = StringHandler.doubleQ(para.getColName());
			matrixUName = para.getUmatrix();
			matrixVName = para.getVmatrix();
		}
	}

	private void calculateCount()
			throws SQLException {
		ResultSet rs;
		String sql;
		sql = "SELECT count(distinct "+StringHandler.doubleQ(para.getColName())+") AS c FROM "+ tableName  + " where " +StringHandler.doubleQ(para.getColName())+ " is not null";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		rs = st.executeQuery(sql.toString());
		if(rs.next()){
			colCount = rs.getLong(1);
		}
		sql = "SELECT count(distinct "+StringHandler.doubleQ(para.getRowName())+") AS c FROM "+ tableName  + " where " +StringHandler.doubleQ(para.getRowName())+ " is not null";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		rs = st.executeQuery(sql.toString());
		if(rs.next()){
			rowCount = rs.getLong(1);
		}
	}

	protected String getSVDSQL() {
		String sql;
		sql = " call alpine_miner_svd_l_proc('"
		+tableName+"','"
		+tempPName+"','"
		+tempQName+"','"
		+mName+"','"
		+nName+"','"
		+StringHandler.doubleQ(value)+"',"
		+para.getNumFeatures()+","
		+initValue+", ?)";
		return sql;
	}

	protected void dropUVTable()
			throws SQLException {
		String sql;
		if (para.getUdrop() != 0){
			 String[] schemaTable=para.getUmatrix().split("\\.", 2);
			 String schema=schemaTable[0];
			 String table=schemaTable[1];

			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+schema+"'"+",'"+table+"')";
//			sql = "DROP TABLE  "+para.getUmatrix();
			itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
			try{
			st.execute(sql);
			}catch(SQLException e){
				e.printStackTrace();
				throw e;
			}
		}
		if (para.getVdrop() != 0){
			 String[] schemaTable=para.getVmatrix().split("\\.", 2);
			 String schema=schemaTable[0];
			 String table=schemaTable[1];

			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+schema+"'"+",'"+table+"')";
			itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
			try{
				st.execute(sql);
				}catch(SQLException e){
					e.printStackTrace();
					throw e;
					
				}
		}
	}

	protected void dropSingularTable()
			throws SQLException {
		String sql;
		if (para.getSingularValueDrop() != 0){
			 String[] schemaTable=para.getSingularValue().split("\\.", 2);
			 String schema=schemaTable[0];
			 String table=schemaTable[1];

			sql = "call PROC_DROPSCHTABLEIFEXISTS( '"+schema+"'"+",'"+table+"')";
			itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
			try{
				st.execute(sql);
				}catch(SQLException e){
					throw e;
					
				}
		}
		
		
		sql = "call PROC_DROPTABLEIFEXISTS( '"+tempUName+"')";//+para.getVmatrix();
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPTABLEIFEXISTS( '"+tempVName+"')";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);

	}


	protected void putBSVDIntoDB() throws SQLException {
		databaseConnection.getConnection().setAutoCommit(false);   
		st = databaseConnection.createStatement(false);
		String sqlCreateU = "create table "+tempUName+" (matrixrow int, matrixcol int, val float)";
		String sqlCreateV = "create table "+tempVName+" (matrixrow int, matrixcol int, val float)";
		String sqlSingularValues = "create table "+para.getSingularValue()+" (\"alpine_feature\" int, "+StringHandler.doubleQ(value)+" float)";
		st.addBatch(sqlCreateU);
		st.addBatch(sqlCreateV);
		st.addBatch(sqlSingularValues);
		addBSVDBatch();
		st.executeBatch();	
		databaseConnection.getConnection().commit();
		databaseConnection.getConnection().setAutoCommit(true);
	}
	protected void dropTempTable()throws SQLException{
		String sql = "call PROC_DROPTABLEIFEXISTS( '"+tempPName+"')";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPTABLEIFEXISTS( '"+tempQName+"')";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPTABLEIFEXISTS( '"+tempUName+"')";//+para.getVmatrix();
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);
		sql = "call PROC_DROPTABLEIFEXISTS( '"+tempVName+"')";
		itsLogger.debug("SVDLanczosDB2.train():sql="+sql);
		st.execute(sql);
		
		
	}
	
}
