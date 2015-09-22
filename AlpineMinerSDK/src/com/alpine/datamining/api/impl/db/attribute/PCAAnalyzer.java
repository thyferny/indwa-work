/**
 * ClassName PCAAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;
/**
 * @author Shawn
 *
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PCAConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.pca.PCADB2;
import com.alpine.datamining.api.impl.db.attribute.pca.PCAGreenplum;
import com.alpine.datamining.api.impl.db.attribute.pca.PCAImpl;
import com.alpine.datamining.api.impl.db.attribute.pca.PCANZ;
import com.alpine.datamining.api.impl.db.attribute.pca.PCAOracle;
import com.alpine.datamining.api.impl.db.attribute.pca.PCAPostgres;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPCA;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.ColumnTypeTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;

public class PCAAnalyzer extends DataOperationAnalyzer {
	private ISqlGeneratorMultiDB sqlGenerator;
	private static Logger logger= Logger.getLogger(PCAAnalyzer.class);
	
	protected Statement st = null;
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource analyticSource)
			throws AnalysisException {
		DatabaseConnection databaseConnection = null;
		PCAConfig pcaConfig = (PCAConfig) analyticSource
				.getAnalyticConfig();
		String anaColumns = pcaConfig.getColumnNames();
		String remainColumns=pcaConfig.getRemainColumns();
		String[] columnsArray = anaColumns.split(",");
		List<String> transformColumns = new ArrayList<String>();
		for(int i = 0; i < columnsArray.length; i++){
			
			transformColumns.add(columnsArray[i]);
		}
		List<String> remainColumnsList = new ArrayList<String>();
		String[] remainColumnsArray = null;
		if(!StringUtil.isEmpty(remainColumns)){
			remainColumnsArray = remainColumns.split(",");
			for(int i = 0; i < remainColumnsArray.length; i++){
				remainColumnsList.add(remainColumnsArray[i]);
			}
		}
		try {
			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) analyticSource,
					analyticSource.getAnalyticConfig());
			filerColumens(dataSet, transformColumns);
			dataSet.computeAllColumnStatistics();
			ColumnTypeTransformer transformer = new ColumnTypeTransformer();
			DataSet newDataSet = transformer.TransformCategoryToNumericRemain(dataSet, transformColumns, remainColumnsList);
			databaseConnection = ((DBTable) dataSet.getDBTable())
					.getDatabaseConnection();
			st = databaseConnection.createStatement(false);
			sqlGenerator = SqlGeneratorMultiDBFactory
			.createConnectionInfo(analyticSource.getDataSourceType());
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
					.createConnectionInfo(analyticSource
							.getDataSourceType());
			String tableName = ((DBTable) newDataSet.getDBTable())
			.getTableName();
			String DBType = dataSourceInfo.getDBType();
			String outSchema=pcaConfig.getPCAQoutputSchema();
			String outTable = pcaConfig.getPCAQoutputTable();
			setOutputTable(outTable);
			String anaType = pcaConfig.getAnalysisType();
			double percent =Double.parseDouble(pcaConfig.getPercent());
			String valueOutTable=pcaConfig.getPCAQvalueOutputTable();
			String valueOutSchema=pcaConfig.getPCAQvalueOutputSchema();
			List<String> newTransformColumns = new ArrayList<String>();
			Columns columns = newDataSet.getColumns();
			Iterator<Column> attributeIter = columns.iterator();
			while(attributeIter.hasNext()){
				Column column = attributeIter.next();
				if((transformColumns.contains(column.getName()) && column.isNumerical()) || !remainColumnsList.contains(column.getName())){
					newTransformColumns.add(column.getName());
				}
			}
			columnsArray = (String[])newTransformColumns.toArray(columnsArray);
			int columnsNumber = columnsArray.length;
			int remainNumber = 0; 
			if(remainColumnsArray != null){
				remainNumber = remainColumnsArray.length;
			}
			PCAImpl implPCA=null;
			ResultSet rs = null;

			if (DBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				implPCA=new PCAOracle();

			} else if (DBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
				implPCA=new PCAGreenplum();

			}

			else if(DBType.
					equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
				implPCA=new PCAPostgres();

			}else if (DBType.equalsIgnoreCase(DataSourceInfoDB2.dBType))
			{
				implPCA=new PCADB2();
			
			}
			else if (DBType.equalsIgnoreCase(DataSourceInfoNZ.dBType))
			{
					implPCA=new PCANZ();
				
			}
			
			Object[] outputResult = new Object[columnsNumber * columnsNumber
					+ 1];
			double PCAMatix[][] = new double[columnsNumber][columnsNumber];
			double QvalueMatix[][] = new double[columnsNumber][columnsNumber];
			double eigenvalues[] = new double[columnsNumber];
			int sortindex[] = new int[columnsNumber];
			
			StringBuffer columnArray = new StringBuffer();
			StringBuffer remainArray = new StringBuffer();
			
			String dropIfExists = "no";
			if(pcaConfig.getPCAQvalueDropIfExist().equalsIgnoreCase("yes"))
			{
				dropIfExists="yes";
			}
			String qValueAppendOnlyString = " ";
			AnalysisStorageParameterModel qValueOutputTableStorageParameterModel = pcaConfig.getPCAQvalueOutputTableStorageParameters();
			if(qValueOutputTableStorageParameterModel == null || !qValueOutputTableStorageParameterModel.isAppendOnly()){
				qValueAppendOnlyString = " ";
			}else{
				qValueAppendOnlyString = sqlGenerator.getStorageString(qValueOutputTableStorageParameterModel.isAppendOnly(), qValueOutputTableStorageParameterModel.isColumnarStorage(), qValueOutputTableStorageParameterModel.isCompression(), qValueOutputTableStorageParameterModel.getCompressionLevel());
			}

			String qValueEndingString = sqlGenerator.setCreateTableEndingSql(qValueOutputTableStorageParameterModel == null ? null: qValueOutputTableStorageParameterModel.getSqlDistributeString()); 

			String qAppendOnlyString = " ";
			AnalysisStorageParameterModel qOutputTableStorageParameterModel = pcaConfig.getPCAQoutputTableStorageParameters();
			if(qOutputTableStorageParameterModel == null || !qOutputTableStorageParameterModel.isAppendOnly()){
				qAppendOnlyString = " ";
			}else{
				qAppendOnlyString = sqlGenerator.getStorageString(qOutputTableStorageParameterModel.isAppendOnly(), qOutputTableStorageParameterModel.isColumnarStorage(), qOutputTableStorageParameterModel.isCompression(), qOutputTableStorageParameterModel.getCompressionLevel());
			}

			String qEndingString = sqlGenerator.setCreateTableEndingSql(qOutputTableStorageParameterModel == null ? null: qOutputTableStorageParameterModel.getSqlDistributeString()); 

			outputResult = implPCA.initPCA(columnsArray, tableName, anaType,
					valueOutTable, valueOutSchema, columnsNumber, st,
					columnArray, rs, dropIfExists,databaseConnection, qValueAppendOnlyString, qValueEndingString);

				int index=0;
				for (int i = 0; i < columnsNumber; i++)
				{
					for (int j = 0; j < columnsNumber; j++) {
						if (outputResult[index] == null)
						{
							String e = SDKLanguagePack.getMessage(SDKLanguagePack.PCA_VALUES_ALL_SAME,pcaConfig.getLocale());
							logger.error(e);
							throw new AnalysisException(e);
						}
						if(i==j)
						{
							double tempNumber=Double.parseDouble(outputResult[index].toString());
							if (implPCA.ValidateConstant(anaType,tempNumber))
							{								
								String e = SDKLanguagePack.getMessage(SDKLanguagePack.PCA_VALUES_ALL_SAME,pcaConfig.getLocale());
								logger.error(e);
								throw new AnalysisException(e);
							};
						}
						
						if(j>=i)
						{
							PCAMatix[i][j] = Double.parseDouble(outputResult[index].toString());
							index++;
						}
						else PCAMatix[i][j]=PCAMatix[j][i];

					}

				}


			ejacobi(PCAMatix, QvalueMatix);

			
			double sumvalues = 0;
			double tempsumvalues = 0;
			for (int i = 0; i < columnsNumber; i++) {
				eigenvalues[i] = PCAMatix[i][i];
				sumvalues = eigenvalues[i] + sumvalues;
			}
			for (int i = 0; i < columnsNumber; i++) {
				sortindex[i] = i + 1;
			}

			mySort(eigenvalues, sortindex,columnsNumber);
			int PCANumber = PcaNumberfind(eigenvalues, percent, columnsNumber);
			if(PCANumber <= 0){
				String e = SDKLanguagePack.getMessage(SDKLanguagePack.PCA_NUM_ZERO,pcaConfig.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}
			
			for (int i = 0; i < PCANumber; i++) {
				String tempqvalues;
				tempqvalues = " " + QvalueMatix[0][sortindex[i] - 1];
				for (int j = 1; j < columnsNumber; j++) {
					tempqvalues = tempqvalues + ","
							+ QvalueMatix[j][sortindex[i] - 1];
				}
				tempsumvalues = tempsumvalues + eigenvalues[i] / sumvalues;
			
				String sql="insert into "
					+StringHandler.doubleQ(valueOutSchema)+"."
					+ StringHandler.doubleQ(valueOutTable)+"  values ("
					+ tempqvalues + "," + i + "," + eigenvalues[i]
					+ "," + eigenvalues[i] / sumvalues + ","
					+ tempsumvalues + ")";
				logger.debug("PCAAnalyzer.doAnalysis:sql="+sql);
				st.execute(sql);
				
			}
			dropIfExists="no";
			if(pcaConfig.getPCAQDropIfExist().equalsIgnoreCase("yes"))
			{
				dropIfExists="yes";
			}
			implPCA.generatePCAResult(remainColumns, remainColumnsArray, tableName, DBType,
					outSchema, outTable, valueOutTable, valueOutSchema,
					remainNumber, st, columnArray, remainArray, dropIfExists,
					PCANumber,databaseConnection, qAppendOnlyString, qEndingString); 

			AnalyzerOutPutPCA localoutput=new AnalyzerOutPutPCA();

			DataBaseInfo localDataBaseInfo = ((DataBaseAnalyticSource)analyticSource).getDataBaseInfo();
			AnalyzerOutPutTableObject PCAResult=getResultTableSampleRow(databaseConnection, localDataBaseInfo, pcaConfig.getPCAQoutputSchema(), pcaConfig.getPCAQoutputTable());
			AnalyzerOutPutTableObject PCAQvalueResult=getResultTableSampleRow(databaseConnection, localDataBaseInfo, pcaConfig.getPCAQvalueOutputSchema(), pcaConfig.getPCAQvalueOutputTable());
			localoutput.setPCAQvalueTables(PCAQvalueResult);
			localoutput.setPCAResultTables(PCAResult);
			localoutput.setAnalyticNodeMetaInfo(createNodeMetaInfo(pcaConfig.getLocale()));
			if(transformer.isTransform())
			{
				implPCA.dropTable(((DBTable) newDataSet.getDBTable())
						.getTableName(),st);
			}
			
			return localoutput;
		} catch (Exception localException) {
			logger.error(localException);
			if (localException instanceof AnalysisError)
				throw ((AnalysisError) localException);
			if (localException instanceof SQLException)
			{
				if(((SQLException)localException).getLocalizedMessage().equalsIgnoreCase("ERROR:  float8div: divide by zero error\n"))
				{								
					String e = SDKLanguagePack.PCA_VALUES_ALL_SAME;
					logger.error(e);
					throw new AnalysisException(e);
				}
			}
			throw new AnalysisException(localException);
		}

	}

 
	private int PcaNumberfind(double[] eigenvalues, double percent,
			int columnsnumber) {
		double sumlamb = 0.0, temp = 0.0;
		int i;
		for (i = 0; i < columnsnumber; i++) {
			sumlamb = sumlamb + eigenvalues[i];
		}
		i = 0;
		while ((temp < percent* sumlamb) && i < columnsnumber) {
			temp = temp + eigenvalues[i];
			i++;
		}
		return i;

	}

	private void mySort(double[] eigenvalues, int[] sortindex, int columnsnumber) {
		int i, j, temp1;
		double temp;

		for (i = 0; i < columnsnumber - 1; i++)
			for (j = 0; j < columnsnumber - i - 1; j++) {
				if (eigenvalues[j] < eigenvalues[j + 1]) {
					temp = eigenvalues[j];
					eigenvalues[j] = eigenvalues[j + 1];
					eigenvalues[j + 1] = temp;
					temp1 = sortindex[j];
					sortindex[j] = sortindex[j + 1];
					sortindex[j + 1] = temp1;
				}
			}

	}



	private int ejacobi(double[][] PCAMatrix, double[][] qvalueMatix) {

		double EPSILON = 1.0e-5;
		double justValue=1.0e-10;
		int i, j, l, iter, ik, jk;
		int N = PCAMatrix.length;
		double aijmax, d, tgphi, cosphi, sinphi, tempo;
		for (i = 0; i < N; i++) {
			for (j = 0; j < N; j++)
				qvalueMatix[i][j] = 0.0;
			qvalueMatix[i][i] = 1.0;
		}
		iter = 0;
		while (iter < N*N/2)//
		{
			aijmax = 0.0;
			ik = 0;
			jk = 0;
			for (i = 0; i < N - 1; i++) {
				for (j = i + 1; j < N; j++) {
					if (Math.abs(PCAMatrix[i][j]) > aijmax) {
						aijmax = Math.abs(PCAMatrix[i][j]);
						ik = i;
						jk = j;
					}
				}
			}
			if (aijmax < EPSILON) {
				break;
			}
			d = (PCAMatrix[ik][ik] - PCAMatrix[jk][jk])
					/ (2.0 * PCAMatrix[ik][jk]);
			
			if (d > 0.0||(Math.abs(d)<justValue))
				tgphi = Math.sqrt(d * d + 1.0) - d;
			else
				tgphi = -Math.sqrt(d * d + 1.0) - d;
			cosphi = 1.0 / Math.sqrt(tgphi * tgphi + 1.0);
			sinphi = tgphi * cosphi;
			tempo = PCAMatrix[ik][ik];
			PCAMatrix[ik][ik] = cosphi * cosphi * tempo + sinphi * sinphi
					* PCAMatrix[jk][jk] + 2.0 * cosphi * sinphi
					* PCAMatrix[ik][jk];
			PCAMatrix[jk][jk] = sinphi * sinphi * tempo + cosphi * cosphi
					* PCAMatrix[jk][jk] - 2.0 * cosphi * sinphi
					* PCAMatrix[ik][jk];
			PCAMatrix[ik][jk] = 0.0;
			PCAMatrix[jk][ik] = 0.0;
			for (l = 0; l < N; l++) {
				if (l != ik && l != jk) {

					PCAMatrix[ik][l] = cosphi * PCAMatrix[l][ik] + sinphi
							* PCAMatrix[l][jk];
					PCAMatrix[jk][l] = -sinphi * PCAMatrix[l][ik] + cosphi
							* PCAMatrix[l][jk];
					PCAMatrix[l][ik] = PCAMatrix[ik][l];
					PCAMatrix[l][jk] = PCAMatrix[jk][l];
				}
			}
			for (l = 0; l < N; l++) {
				tempo = qvalueMatix[l][ik];
				qvalueMatix[l][ik] = cosphi * tempo + sinphi
						* qvalueMatix[l][jk];
				qvalueMatix[l][jk] = -sinphi * tempo + cosphi
						* qvalueMatix[l][jk];
			}
			iter++;
		}
		return iter;

	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.PCA_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.PCA_DESCRIPTION,locale));

		return nodeMetaInfo;
	}


	
	
	
}
