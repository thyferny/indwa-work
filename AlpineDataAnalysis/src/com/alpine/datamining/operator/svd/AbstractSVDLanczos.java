package com.alpine.datamining.operator.svd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.tools.matrix.SingularValueDecomposition;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public abstract class AbstractSVDLanczos {
    private static final Logger logger = Logger.getLogger(AbstractSVDLanczos.class);
    protected SVDParameter para;
	protected DatabaseConnection databaseConnection;
	protected IDataSourceInfo dataSourceInfo = null;
	protected ISqlGeneratorMultiDB sqlGenerator;
	protected double initValue = 0;
	protected long colCount = 0;
	protected long rowCount = 0;
	protected String mName = "";
	protected String nName = "";
	protected String matrixUName = "";
	protected String matrixVName = "";
	protected String tableName;
	protected String value;
	protected Matrix U = null;
	protected Matrix V = null;
	protected double[] singularValues = null;
	protected Statement st = null;
	protected ResultSet rs = null;
	protected String tempPName;
	protected String tempQName;
	protected String tempUName;
	protected String tempVName;
	protected AnalysisStorageParameterModel uStorageParameters;
	protected AnalysisStorageParameterModel vStorageParameters;
	protected AnalysisStorageParameterModel sStorageParameters;
	protected boolean uAppendOnly = false;
	protected boolean vAppendOnly = false;
	protected boolean sAppendOnly = false;

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
			tempPName =  "p"+System.currentTimeMillis();
			tempQName =  "q"+System.currentTimeMillis();
			tempUName =  "u"+System.currentTimeMillis();
			tempVName =  "v"+System.currentTimeMillis();

			value = dataSet.getColumns().getLabel().getName();
			sql = getSVDSQL();

			trainGetBSVD(sql);
			dropSingularTable();
			st.close();
			putBSVDIntoDB();
			st.close();
			st = databaseConnection.createStatement(false);
			dropUVTable();
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

	protected abstract  String getSVDSQL();

	protected abstract void dropUVTable()
			throws SQLException;

	protected abstract void dropSingularTable()
			throws SQLException ;

	protected void createUVTable()
			throws SQLException {
		String sqlU = "create table "+matrixUName+(uAppendOnly ? sqlGenerator.getStorageString(uStorageParameters.isAppendOnly(), uStorageParameters.isColumnarStorage(), uStorageParameters.isCompression(), uStorageParameters.getCompressionLevel()):" ")+" as select "+tempPName+".m_column as "+mName+", "+tempUName+".matrixcol as \"alpine_feature\" , sum("+tempPName+".val * "+tempUName+".val) as "+StringHandler.doubleQ(value)+" from "+tempPName+" join "+tempUName+" on "+tempPName+".n_column = "+tempUName+".matrixrow group by "+tempPName+".m_column, "+tempUName+".matrixcol "+sqlGenerator.setCreateTableEndingSql(uStorageParameters == null ? null : uStorageParameters.getSqlDistributeString()); 
		logger.debug("SVDLanczos.train():sql=" + sqlU);
		st.execute(sqlU);
		String sqlV = "create table "+matrixVName+(vAppendOnly ? sqlGenerator.getStorageString(vStorageParameters.isAppendOnly(), vStorageParameters.isColumnarStorage(), vStorageParameters.isCompression(), vStorageParameters.getCompressionLevel()):" ")+" as select "+tempQName+".n_column as "+nName+" , "+tempVName+".matrixcol as \"alpine_feature\" , sum("+tempQName+".val * "+tempVName+".val) as "+StringHandler.doubleQ(value)+" from "+tempQName+" join "+tempVName+" on "+tempQName+".m_column = "+tempVName+".matrixrow group by "+tempQName+".n_column, "+tempVName+".matrixcol "+sqlGenerator.setCreateTableEndingSql(vStorageParameters == null ? null : vStorageParameters.getSqlDistributeString()); 
		logger.debug("SVDLanczos.train():sql=" + sqlV);
		st.execute(sqlV);
	}
	protected abstract void dropTempTable()throws SQLException;
	protected abstract void putBSVDIntoDB() throws SQLException;

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
		ResultSet rs;
		logger.debug("SVDLanczos.train():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		Object[] result = null;
		if(rs.next()){
			result = (Object[])rs.getArray(1).getArray();
		}
		if (result.length%2 != 0){
			logger.error("SVDLanczos.train():result length Wrong: " + result.length);
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
		logger.debug("SVDLanczos.train():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		if(rs.next()){
			colCount = rs.getLong(1);
		}
		sql = "SELECT count(distinct "+StringHandler.doubleQ(para.getRowName())+") AS c FROM "+ tableName  + " where " +StringHandler.doubleQ(para.getRowName())+ " is not null";
		logger.debug("SVDLanczos.train():sql=" + sql);
		rs = st.executeQuery(sql.toString());
		if(rs.next()){
			rowCount = rs.getLong(1);
		}
	}
}
