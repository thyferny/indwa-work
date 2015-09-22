/**
 * ClassName KMeansDBFunctionPara.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.kmeans;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.ColumnTypeTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * This operator represents an implementation of k-means. This operator will
 * create a cluster column if not present yet.
 * 
 * @author Eason Yu,Jeff Dong
 */
public class KMeansDBFunctionPara extends AbstractClusterer {
    private static Logger itsLogger= Logger.getLogger(KMeansDBFunctionPara.class);
    private static final String[] PARAMETER_DISTANCE_TYPE = { "Euclidean",
			"GeneralizedIDivergence", "KLDivergence", "CamberraNumerical",
			"Manhattan", "CosineSimilarity", "DiceNumericalSimilarity",
			"InnerProductSimilarity", "JaccardNumericalSimilarity"};

	private long startTime;
	private long endTime;
	private HashMap<String, Integer> distance_map = new HashMap<String, Integer>();
	private static final int lineThrethhold=AlpineDataAnalysisConfig.KMEANS_ARRAY_THRESHOLD;
	
	private KmeansParameter para;
	
	private boolean useArray = false;

	private IKmeansDB kmeansdb;
	private IDataSourceInfo  dataSourceInfo;
	ISqlGeneratorMultiDB sqlGenerator;
	ColumnTypeTransformer transformer = new ColumnTypeTransformer();

	private String copyTableName = null; 
	
	public KMeansDBFunctionPara() {
		super();
		for (int i = 0; i < PARAMETER_DISTANCE_TYPE.length; i++) {
			distance_map.put(PARAMETER_DISTANCE_TYPE[i], i + 1);
		}	
	}

	public ClusterModel generateClusterModel(DataSet dataSet)
			throws OperatorException {
		para = (KmeansParameter)getParameter();
		dataSourceInfo=DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		
		sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		
		// Get parameters from SDK. 
		int k = para.getK();
		if(dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)||
				dataSourceInfo.getDBType().equals(DataSourceInfoNZ.dBType)){
			useArray = false;
		}else{
			useArray = para.isUseArray();
		}
		int splitNumber = para.getSplitNumber();
		int maxOptimizationSteps = para.getMaxOptimizationSteps();
		int maxRuns = para.getMaxRuns();
		int distance = distance_map
				.get(para.getDistance());
		kmeansdb=KmeansFactory.createKmeansDB(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		kmeansdb.setUseArray(useArray);

		if (dataSet.size() < k) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.DATASET_TOO_SMALL_PARA, k);
		}

		//Get parameters from old dataSet.
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		Columns columns = dataSet.getColumns();
		
		String id=null;
		String tempid=null;
		if(dataSet.getColumns().getId()!=null)
		{
			 id = StringHandler.doubleQ(dataSet.getColumns().getId().getName());
			//judge whether the id column is distinct.If not,throw user error.
			 isDistinct(databaseConnection, id, tableName);
		}else
		{
			tempid="alpineid"+System.currentTimeMillis();
		}

		String clusterColumnName=StringHandler.doubleQ(para.getClusterColumnName());
		
		itsLogger.debug(
				"KMeansDBFunctionPara  add connection: ="
						+ databaseConnection.getConnection());
		
		
		HashMap<String, Long> clustermap = new HashMap<String, Long>();	
		HashMap<String, String> clusterExchangeMap_oldkey = new HashMap<String, String>();
		List<String> clusterNameList = new ArrayList<String>();
		ClusterModel bestModel = new ClusterModel(k);
		
		bestModel.setSchemaName(para.getResultSchema());
		bestModel.setTableName(para.getResultTableName());
		
		//judge whether the id column is distinct.If not,throw user error.
//		isDistinct(databaseConnection, id, tableName);

		//Transform Category column to numeric
		transformer.setUseArray(useArray);
		DataSet newDataSet = transformer
				.TransformCategoryToNumeric_new(dataSet);

		//Get parameters from new dataSet.
		Columns atts_new = newDataSet.getColumns();
		String newTableName = ((DBTable) newDataSet
				.getDBTable()).getTableName();
		int columnNum = newDataSet.getColumns().size();
		
		boolean maxColumnFlag=ifMaxColumn(newDataSet,k);
		
		
		
		String columnNameString = generateColumnNameString(atts_new);
		String columnArrayString=null;
		if(dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
			columnArrayString = generateColumnArrayOracleString(dataSet.getColumns());
		}else if(dataSourceInfo.getDBType().equals(DataSourceInfoPostgres.dBType) ||
		dataSourceInfo.getDBType().equals(DataSourceInfoGreenplum.dBType)){
			columnArrayString = generateColumnArrayString(dataSet.getColumns());
		}else if(dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)){
			columnArrayString = generateColumnArrayDB2String(newDataSet.getColumns());
		}else if(dataSourceInfo.getDBType().equals(DataSourceInfoNZ.dBType)){
//			columnArrayString = generateColumnNZString(newDataSet);
		}
		
		ArrayList<Integer> columnArrayList = generateColumnArrayList(dataSet.getColumns());
		int columnArraySum = caculateColumnArraySum(dataSet.getColumns());
		

			startTime = System.currentTimeMillis();

			itsLogger.debug("---start caculate center point------");
			String temptablename =  "A"+System.currentTimeMillis();//"AlpineKMeans" +
			String copyTableName=generateCopyTable(dataSet, databaseConnection, tableName,temptablename, tempid,
					newDataSet);
			String resultTableName = generateCenterPoint(newDataSet,columns, k,
					maxOptimizationSteps, maxRuns, databaseConnection,tableName,
					newTableName, temptablename, copyTableName,id,tempid,clusterColumnName,columnNameString,columnArrayString,
					columnNum, distance, bestModel,
					clusterExchangeMap_oldkey,clustermap,clusterNameList,maxColumnFlag);
			endTime = System.currentTimeMillis();
			itsLogger.debug("---end caculate center point------");
			itsLogger.debug("---time-consuming:" + (endTime - startTime) + "ms---");

			startTime = System.currentTimeMillis();
			itsLogger.debug("---start deal result------");

			k=clusterExchangeMap_oldkey.keySet().size();

			dealCenterPoint(newDataSet,databaseConnection, atts_new,temptablename, k, columnNum, bestModel, clusterExchangeMap_oldkey,maxColumnFlag, columnArrayList, dataSet.getColumns());

			String isNotNull=generateIsNotNullSql(columns);
			
			existNullValue(databaseConnection,columns,tableName,bestModel);
			
			String min_diff_sql = generateMinDiffSql(tableName, splitNumber,
					columns,isNotNull);

			
			HashMap<String, Float> min_Map = new HashMap<String, Float>();
			HashMap<String, Float> diff_Map = new HashMap<String, Float>();
			
			caculateMinDiff(databaseConnection, columns, min_Map, diff_Map,
					min_diff_sql);

			HashMap<String, Long[]> count_Map = new HashMap<String, Long[]>();
			caculateCountForEachCluster(
					tableName, databaseConnection,
					columns, splitNumber, min_Map, diff_Map, count_Map,isNotNull);

			dealResult(dataSet, tableName, k, bestModel, splitNumber,
					columnArraySum, clusterColumnName,clustermap, clusterNameList,
					resultTableName, columns, min_Map, diff_Map,
					count_Map,isNotNull);
			
			endTime = System.currentTimeMillis();
			itsLogger.debug("---end deal result------");
			itsLogger.debug("---time-consuming:" + (endTime - startTime) + "ms---");
			if(transformer.isTransform())
			{
				IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) newDataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); ; 
				Statement st = null;
				try {
					st = ((DBTable) newDataSet.getDBTable()).getDatabaseConnection().createStatement(false);
					String tempTableName = ((DBTable) newDataSet.getDBTable()).getTableName();
					multiDBUtility.dropTraingTempTable(st, tempTableName);
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
			try {
				Statement st = ((DBTable) newDataSet.getDBTable()).getDatabaseConnection().createStatement(false);
				kmeansdb.dropTemp(st, temptablename, this.copyTableName);
				st.close();
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
			}
			
			
		return bestModel;
	}
	
	private String generateColumnArrayDB2String(Columns columns) {
		Iterator<Column> atts_new_i = columns.iterator();
		int count = 0;
		String columnNameList = "";
		while (atts_new_i.hasNext()) {
			String columnName = atts_new_i.next().getName();
			if (count != columns.size() - 1) {
				columnNameList +=  columnName + ",";
			} else {
				columnNameList +=  columnName;
			}
			count++;
		}
		return columnNameList;
	}

	private String generateColumnArrayOracleString(Columns columns) {
		ArrayList<String> columnStringList=new ArrayList<String>();
		Iterator<Column> ii = columns.iterator();
		while (ii.hasNext()) {
			Column column = ii.next();
			if(column.isNumerical()){
				columnStringList.add("0");
			}else{
				int i = 0;
				for(int j = 0; j < column.getMapping().size(); j++){
					if(transformer.getAllTransformMap_valueKey().get(column.getName()) != null && transformer.getAllTransformMap_valueKey().get(column.getName()).containsKey(column.getMapping().mapIndex(j))){
						i++;
					}
				}
				columnStringList.add(String.valueOf(i));
			}
		}
		StringBuffer sbf=CommonUtility.array2OracleArray(columnStringList, CommonUtility.OracleDataType.Integer);
		return sbf.toString();
		
	}

	private String generateCopyTable(DataSet dataSet,
			DatabaseConnection databaseConnection, String tableName,String tempTableName, String tempid,
			DataSet newDataSet)
			throws OperatorException {
		String copyTableName;
		if(!newDataSet.equals(dataSet)&&tempid!=null)
		{
			copyTableName="tablecopy"+System.currentTimeMillis();
			this.copyTableName = copyTableName;
			if(dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)){
				StringBuilder selectSql = kmeansdb.generateCreateCopyTableSql(tableName,
						tempid, copyTableName);
				StringBuilder createSql=new StringBuilder();
				createSql.append("create table ").append(copyTableName);
				createSql.append(" as (").append(selectSql).append(")").append(sqlGenerator.setCreateTableEndingSql(null));
				
				StringBuilder insertSql=new StringBuilder();
				insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
						copyTableName));
				try {
					Statement st=databaseConnection.createStatement(false);
					itsLogger.debug("generateCopyTable():sql="+createSql);
					st.execute(createSql.toString());
					
					if (insertSql.length() > 0) {
						st.execute(insertSql.toString());
						itsLogger.info(
								"generateCopyTable():insertTableSql="
										+ insertSql);
					}			
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else{
				StringBuilder sb_createCopy = kmeansdb.generateCreateCopyTableSql(tableName,
						tempid, copyTableName);
				
				DatabaseUtil.alterParallel(databaseConnection,"TABLE");//for oracle
				try {
					Statement st=databaseConnection.createStatement(false);
					itsLogger.debug("generateCopyTable():sql="+sb_createCopy);
					st.execute(sb_createCopy.toString());
					st.close();
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
		}else if(!newDataSet.equals(dataSet))
		{
			copyTableName=tableName;
		}else
		{
			copyTableName=tempTableName+"copy";
		}
		return copyTableName;
	}


	private boolean ifMaxColumn(DataSet dataSet,int k) 
	{
		int columnCount =dataSet.getColumns().size();
		
		if(columnCount*k>Integer.parseInt(AlpineDataAnalysisConfig.MAX_COLUMN))
		{
			return true;
		}else
		{
			return false;
		}	
	}

	private void existNullValue(DatabaseConnection databaseConnection,Columns atts,String tableName,ClusterModel bestModel) throws OperatorException {

		Iterator<Column> i=atts.iterator();
		StringBuilder sb_isNotNull=new StringBuilder(" select ");
		while(i.hasNext())
		{
			Column att=i.next();
			sb_isNotNull.append(" sum(case when ").append(StringHandler.doubleQ(att.getName())).append(" is null then 1 else 0 end) ");
			sb_isNotNull.append(StringHandler.doubleQ(att.getName())).append(",");
		}
		sb_isNotNull=sb_isNotNull.deleteCharAt(sb_isNotNull.length()-1);
		sb_isNotNull.append(" from ").append(tableName);
		
		try {
			Statement st=databaseConnection.createStatement(false);
			itsLogger.debug("KMeansDBFunctionPara.existNullValue():sql="+sb_isNotNull);
			ResultSet rs=st.executeQuery(sb_isNotNull.toString());
			StringBuilder sb=new StringBuilder();
			while(rs.next())
			{
				for(int j=0;j<rs.getMetaData().getColumnCount();j++)
				{
					if(rs.getLong(j+1)!=0)
					{
						sb.append(StringHandler.doubleQ(rs.getMetaData().getColumnName(j+1))).append(",");
					}
				}
			}
			if(sb.length()!=0)
			{
				sb=sb.deleteCharAt(sb.length()-1);
				itsLogger.debug("table "+tableName+" "+sb+" columns exists null values");
				bestModel.setIsStable(false);	
				String table_exist_null=AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.TABLE_EXIST_NULL, AlpineThreadLocal.getLocale());
				String[] temp=table_exist_null.split(";");
				bestModel.setStableInformation(temp[0]+sb.toString()+temp[1]+Tools.getLineSeparator());
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private String generateIsNotNullSql(Columns columns) {
		Iterator<Column> i=columns.iterator();
		StringBuilder sb_isNotNull=new StringBuilder();
		while(i.hasNext())
		{
			Column att=i.next();
			sb_isNotNull.append(StringHandler.doubleQ(att.getName())).append(" is not null and ");
		}
		sb_isNotNull=sb_isNotNull.delete(sb_isNotNull.length()-4, sb_isNotNull.length()-1);
		return sb_isNotNull.toString();
	}

	private void dealCenterPoint(DataSet dataSet,DatabaseConnection databaseConnection,
			Columns atts_new, String temptableName, int k, int columncount,
			ClusterModel bestModel,Map<String, String> clusterExchangeMap,boolean maxColumnFlag, ArrayList<Integer> columnArrayList, Columns oldAtt) 
		throws OperatorException {
		ArrayList<ArrayList<String>> centerPointList = new ArrayList<ArrayList<String>>();

		Iterator<Column> ii = atts_new.iterator();
		ArrayList<String> centerPointName = new ArrayList<String>();
		centerPointName.add("Cluster");
		if (!useArray){
			while (ii.hasNext()) {
				Column att = ii.next();
				centerPointName.add(att.getName());
			}
		}else{
			getArrayCenterPointName(oldAtt, centerPointName);
		}
		centerPointList.add(centerPointName);
		try {
			StringBuilder sb = new StringBuilder("select * from ");
			sb.append(temptableName).append("result1");
			Statement st = databaseConnection.createStatement(false);
			itsLogger.debug("KMeansDBFunctionPara.dealCenterPoint():sql="+sb);
			ResultSet rs = st.executeQuery(sb.toString());
			rs.next();
			int i_i=0;
			if(useArray &&dataSourceInfo.getDBType().equals(DataSourceInfoOracle.dBType)){
				columncount=0;
				Iterator<Integer> iter=columnArrayList.iterator();
				while(iter.hasNext()){
					int count=iter.next();
					if(count==0){
						columncount++;
					}else{
						columncount=columncount+count;
					}
				}
				for (int i = 0; i < k; i++)// each cluster
				{
					ArrayList<String> centerPointForEachCluster = new ArrayList<String>();
					while(clusterExchangeMap.get(String.valueOf(i_i))==null)
					{
						i_i++;
					}
					centerPointForEachCluster.add(clusterExchangeMap.get(String.valueOf(i_i)));			
					for (int j = 0; j < columncount; j++)// each column
					{
							kmeansdb.dealResult(dataSet, columncount, maxColumnFlag, rs, i_i,
									centerPointForEachCluster, j,lineThrethhold);
					}
					centerPointList.add(centerPointForEachCluster);
					i_i++;
				}
			}else{
				for (int i = 0; i < k; i++)// each cluster
				{
					ArrayList<String> centerPointForEachCluster = new ArrayList<String>();
					while(clusterExchangeMap.get(String.valueOf(i_i))==null)
					{
						i_i++;
					}
					centerPointForEachCluster.add(clusterExchangeMap.get(String.valueOf(i_i)));			
					for (int j = 0; j < columncount; j++)// each column
					{
						if (useArray && columnArrayList.get(j) >= 1
								&&(dataSourceInfo.getDBType().equals(DataSourceInfoPostgres.dBType)||
								dataSourceInfo.getDBType().equals(DataSourceInfoGreenplum.dBType))){
							for(int l = 0; l < columnArrayList.get(j); l++){
								kmeansdb.dealResult(dataSet, columncount, maxColumnFlag, rs, i_i+l*k,
										centerPointForEachCluster, j,lineThrethhold);
							}
						}else{
							kmeansdb.dealResult(dataSet, columncount, maxColumnFlag, rs, i_i,
									centerPointForEachCluster, j,lineThrethhold);
						}
					}
					centerPointList.add(centerPointForEachCluster);
					i_i++;
				}
			}

			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		bestModel.setCenterPoint(centerPointList);
	}


	private String generateColumnNameString(Columns atts_new) {
		Iterator<Column> atts_new_i = atts_new.iterator();
		int count = 0;
		String columnNameList = "";
		while (atts_new_i.hasNext()) {
			String columnName = atts_new_i.next().getName();
			if (count != atts_new.size() - 1) {
				columnNameList += "'" + columnName + "',";
			} else {
				columnNameList += "'" + columnName + "'";
			}
			count++;
		}
		return columnNameList;
	}
	
	private int caculateColumnArraySum(Columns attsOld) {
		int sum = 0;
		Iterator<Column> ii = attsOld.iterator();
//		int i = 0;
		while (ii.hasNext()) {
			Column column = ii.next();
			if(column.isNumerical()){
				sum++;
			}else{
				for(int j = 0; j < column.getMapping().size(); j++){
					if(transformer.getAllTransformMap_valueKey().get(column.getName()) != null && transformer.getAllTransformMap_valueKey().get(column.getName()).containsKey(column.getMapping().mapIndex(j))){
						sum++;
					}
				}
			}
		}
		return sum;
	}
	
	private String generateColumnArrayString(Columns attsOld) {
		StringBuffer columnArrayString = new StringBuffer();
		Iterator<Column> ii = attsOld.iterator();
//		int i = 0;
		boolean first = true;
		while (ii.hasNext()) {
			Column column = ii.next();
			if (first){
				first = false;
			}else{
				columnArrayString.append(",");
			}

			if(column.isNumerical()){
				columnArrayString.append(0);
			}else{
				int i = 0;
				for(int j = 0; j < column.getMapping().size(); j++){
					if(transformer.getAllTransformMap_valueKey().get(column.getName()) != null && transformer.getAllTransformMap_valueKey().get(column.getName()).containsKey(column.getMapping().mapIndex(j))){
						i++;
					}
				}
				columnArrayString.append(i);
			}
		}
		return columnArrayString.toString();
	}
	private ArrayList<Integer> generateColumnArrayList(Columns attsOld) {
		ArrayList<Integer> result = new ArrayList<Integer>(); 
		Iterator<Column> ii = attsOld.iterator();
//		int i = 0;
		while (ii.hasNext()) {
			Column column = ii.next();
			if(column.isNumerical()){
				result.add(0);
			}else{
				int i = 0;
				for(int j = 0; j < column.getMapping().size(); j++){
					if(transformer.getAllTransformMap_valueKey().get(column.getName()) != null && transformer.getAllTransformMap_valueKey().get(column.getName()).containsKey(column.getMapping().mapIndex(j))){
						i++;
					}
				}
				result.add(i);
			}
		}
		return result;
	}
	private void getArrayCenterPointName(Columns attsOld, ArrayList<String> centerPointName){
		Iterator<Column> ii = attsOld.iterator();
		while (ii.hasNext()) {
			Column column = ii.next();
			if(column.isNumerical()){
				centerPointName.add(column.getName());
			}else{
					for(int j = 0; j < column.getMapping().size(); j++){
						if(transformer.getAllTransformMap_valueKey().get(column.getName()) != null && transformer.getAllTransformMap_valueKey().get(column.getName()).containsKey(column.getMapping().mapIndex(j))){
							centerPointName.add(transformer.getAllTransformMap_valueKey().get(column.getName()).get(column.getMapping().mapIndex(j)));
						}
					}
			}
		}
	}
	private void caculateMinDiff(DatabaseConnection databaseConnection,
			Columns columns, HashMap<String, Float> min_Map,
			HashMap<String, Float> diff_Map, String min_diff_sql)
			throws OperatorException {
		try {
			Statement st = databaseConnection.createStatement(false);
			if (min_diff_sql == null) {
				return;
			}
			itsLogger.debug("KMeansDBFunctionPara.caculateMinDiff():sql="+min_diff_sql);
			ResultSet rs = st.executeQuery(min_diff_sql);
			rs.next();
			Iterator<Column> min_diff_map_i = columns.iterator();
			int count = 0;
			while (min_diff_map_i.hasNext()) {
				Column att = min_diff_map_i.next();
				if (!att.isNumerical()) {
					continue;
				}
				min_Map.put(att.getName(), rs.getFloat(count * 2 + 1));
				diff_Map.put(att.getName(), rs.getFloat(count * 2 + 2));
				count++;
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private void dealResult(DataSet dataSet, String tableName,
			int k, ClusterModel bestModel, int split_Number, int columnNum,String clusterColumnName,
			HashMap<String, Long> clustermap,
			List<String> clusterNameList,
			String resultTableName, Columns columns,
			HashMap<String, Float> min_Map, HashMap<String, Float> diff_Map,
			HashMap<String, Long[]> countMap,String isNotNull) throws OperatorException {
		int count;
		try {
			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			Statement st = databaseConnection.createStatement(false);
			ResultSet rs;
			bestModel.generateClustersArrays(columnNum);
			DecimalFormat df = new DecimalFormat("#.##");
			Iterator<Column> result_i = columns.iterator();
			count = 0;
			while (result_i.hasNext()) {
				Column att = result_i.next();
				String[] a = new String[k + 3];
				a[0] = att.getName();
				if (att.isNumerical()) {
					StringBuilder sb_interval = new StringBuilder();
					for (int j = 0; j < split_Number; j++) {
						sb_interval.append(
								df.format(min_Map.get(a[0])
										+ diff_Map.get(a[0]) * (j))).append(
								" - ");
						sb_interval.append(df.format(min_Map.get(a[0])
								+ diff_Map.get(a[0]) * (j + 1)));
						if (j != split_Number - 1) {
							sb_interval.append(",");
						}
					}
					a[1] = sb_interval.toString();
					long sum = 0;
					Long[] split_count = countMap.get(a[0]);
					for (int j = 0; j < split_Number; j++) {
						sum = sum + split_count[j];
						if (j == 0) {
							a[2] = String.valueOf(split_count[j]);
						} else {
							a[2] += "," + String.valueOf(split_count[j]);
						}
						if (j == split_Number - 1) {
							a[2] = String.valueOf(sum) + ";" + a[2];
						}
					}
					for (int m = 0; m < k; m++) {
						a[m + 3] = clusterNameList.get(m)
								+ ",Size: "
								+ String.valueOf(clustermap.get(clusterNameList
										.get(m))) + ";";
						StringBuilder sb_count=new StringBuilder("select ");
						for (int n = 0; n < split_Number; n++) {
							sb_count.append(" sum(case when ").append(clusterColumnName).append("='").append(clusterNameList.get(m));
							sb_count.append("' and ").append(StringHandler.doubleQ(a[0]));
							if (n == 0) {
								sb_count.append("<=");
								sb_count.append(min_Map.get(a[0]) + diff_Map.get(a[0])* (n + 1));
								sb_count.append(" then 1 else 0 end),");
							} else if (n == split_Number - 1) {
								sb_count.append(">");
								sb_count.append(min_Map.get(a[0]) + diff_Map.get(a[0])* n);
								sb_count.append(" then 1 else 0 end)");
							} else {
								sb_count.append(">");
								sb_count.append(min_Map.get(a[0]) + diff_Map.get(a[0])* n).append(" and ");
								sb_count.append(StringHandler.doubleQ(a[0])).append("<=");
								sb_count.append(min_Map.get(a[0])+diff_Map.get(a[0])*(n + 1));
								sb_count.append(" then 1 else 0 end),");
							}
						}
						sb_count.append(" from ").append(resultTableName);
						itsLogger.debug("KMeansDBFunctionPara.dealResult():sql="+sb_count);
						rs = st.executeQuery(sb_count.toString());
						while(rs.next())
						{
							a[m + 3]+=String.valueOf(rs.getLong(1));
							for(int i=1;i<split_Number;i++)
							{
								a[m + 3] += ","+ String.valueOf(rs.getLong(i+1));		
							}
						}
						rs.close();
					}
					bestModel.setClustersArrays(a);
				} else// category column
				{
					List<String> valueList = att.getMapping().getValues();
					Iterator<String> valueList_i = valueList.iterator();

					count = 0;
					st = databaseConnection.createStatement(false);
					a[1] = "";
					a[2] = "";
					long sum = 0;
					while (valueList_i.hasNext()) {

						String value = valueList_i.next();
						if (count != valueList.size() - 1) {
							a[1] += value + ",";
						} else {
							a[1] += value;
						}

						value = value.replace("'", "''");
						StringBuilder sb_select = new StringBuilder(
								"select count(");
						sb_select.append(StringHandler.doubleQ(a[0])).append(") from ")
						.append(tableName);
						sb_select.append(" where ").append(StringHandler.doubleQ(a[0]))
								.append("=")
								.append(CommonUtility.quoteValue(dataSourceInfo.getDBType(), att, value))
//								.append("'").append(value).append("'")
								.append(" and ").append(isNotNull);
//								.append("='").append(value).append("'").append(" and ").append(isNotNull);
						itsLogger.debug("KMeansDBFunctionPara.dealResult():sql="+sb_select);
						rs = st.executeQuery(sb_select.toString());
						rs.next();
						if (count != valueList.size() - 1) {
							a[2] += rs.getLong(1) + ",";
							sum = sum + rs.getLong(1);
						} else {
							sum = sum + rs.getLong(1);
							a[2] = sum + ";" + a[2] + rs.getLong(1);

						}
						rs.close();
						for (int m = 0; m < k; m++) {
							a[m + 3] = clusterNameList.get(m)
									+ ",Size: "
									+ String.valueOf(clustermap
											.get(clusterNameList.get(m))) + ";";
							Iterator<String> valueList_ii = valueList
									.iterator();
							while (valueList_ii.hasNext()) {
								String value_each_cluster = valueList_ii.next();
								value_each_cluster = StringHandler.escQ(value_each_cluster);
								StringBuilder sb_select_each_cluster = new StringBuilder(
										"select count(*) from ");
								sb_select_each_cluster.append(resultTableName)
										.append(" where ").append(clusterColumnName).append("='").append(
												clusterNameList
																.get(m))
										.append("' and ").append(StringHandler.doubleQ(a[0])).append(
												"=")
										.append(CommonUtility.quoteValue(dataSourceInfo.getDBType(), att, value_each_cluster));
//												.append("'")
//										.append(value_each_cluster).append("'");
								itsLogger.debug("KMeansDBFunctionPara.dealResult():sql="+sb_select_each_cluster);
								rs = st.executeQuery(sb_select_each_cluster
										.toString());
								if (rs.next())
									a[m + 3] += ","
											+ String.valueOf(rs.getLong(1));
								else
									a[m + 3] += ",0";
								rs.close();
							}
							a[m + 3] = a[m + 3].replaceFirst(";,", ";");
						}
						count++;
					}
					bestModel.setClustersArrays(a);
				}
			}
			st.close();
			bestModel.setClusterColumn(clusterColumnName);
			bestModel.setDataSet(dataSet);
			bestModel.setResultTableName(resultTableName);
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private void caculateCountForEachCluster(
			String tableName, DatabaseConnection databaseConnection,
			Columns columns, int split_Number,
			HashMap<String, Float> min_Map, HashMap<String, Float> diff_Map,
			HashMap<String, Long[]> count_Map,String isNotNull) throws OperatorException {
		Iterator<Column> count_i = columns.iterator();
		int count = 0;
		StringBuilder sb_count = new StringBuilder();
		while (count_i.hasNext()) {
			Column att = count_i.next();
			if (!att.isNumerical())
				continue;
			String attName = StringHandler.doubleQ(att.getName());
			Long[] split_count = new Long[split_Number];
			for (int j = 0; j < split_Number; j++) {
				sb_count.setLength(0);
				sb_count.append("select count(").append(attName)
						.append(") from ")
						.append(tableName);
				sb_count.append(" where ").append(attName);
				if (j == 0) {
					sb_count.append("<=").append(
							min_Map.get(att.getName()) + diff_Map.get(att.getName()));
				} else if (j == split_Number - 1) {
					sb_count.append(">").append(
							min_Map.get(att.getName()) + diff_Map.get(att.getName()) * j);
				} else {
					sb_count.append(">").append(
							min_Map.get(att.getName()) + diff_Map.get(att.getName()) * j);
					sb_count.append(" and ").append(attName).append("<=")
							.append(
									min_Map.get(att.getName())
											+ diff_Map.get(att.getName()) * (j + 1));
				}
				sb_count.append(" and ").append(isNotNull);
				itsLogger.debug("KMeansDBFunctionPara.caculateCountForEachCluster():sql="+sb_count.toString());
				try {
					Statement st = databaseConnection.createStatement(false);
					ResultSet rs = st.executeQuery(sb_count.toString());
					rs.next();
					split_count[j] = rs.getLong(1);
					rs.close();
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
			count++;
			count_Map.put(att.getName(), split_count);
		}
	}

	private String generateMinDiffSql(String tableName, int split_Number,
			Columns columns,String isNotNull) {
		StringBuilder sb_min_diff = new StringBuilder("select ");
		Iterator<Column> min_diff_i = columns.iterator();
		StringBuilder sb_temp = new StringBuilder("");
		while (min_diff_i.hasNext()) {
			Column att = min_diff_i.next();
			if (!att.isNumerical()) {
				continue;
			}
			String columnName = StringHandler.doubleQ(att.getName());
			sb_temp.append(" min(").append(columnName).append("),");
			sb_temp.append(" (max(").append(sqlGenerator.castToDouble(columnName)).append(")");
			sb_temp.append("-min(").append(columnName)
				.append("))/");
			sb_temp.append(new Double(split_Number)).append(",");
		}
		if (sb_temp.length() == 0) {
			return null;
		}
		sb_temp = sb_temp.deleteCharAt(sb_temp.length() - 1);
		sb_min_diff.append(sb_temp).append(" from ")
				.append(tableName).append(" where ").append(isNotNull);

		itsLogger.debug("KMeansDBFunctionPara.generateMinDiffSql():sql="+sb_min_diff.toString());
		return sb_min_diff.toString();
	}

	private String generateCenterPoint(DataSet dataSet,Columns atts, int k,
			int maxOptimizationSteps, int maxRuns,
			DatabaseConnection databaseConnection,String tableName,
			String newTableName, String temptablename,String copyTableName,String id,String tempid,String clusterColumnName,
			String columnNameList, String columnArrayList,int columnsSize, int distance,
			ClusterModel bestModel,
			HashMap<String,String> clusterExchangeMap_oldKey,
			HashMap<String, Long> clustermap,List<String> clusterNameList,
			boolean maxColumnFlag) throws OperatorException {
		String resulttableName =null;
		try {
			Statement st = databaseConnection.createStatement(false);
			ResultSet rs =null;
			if(dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)){
				StringBuilder sql=new StringBuilder("");
				sql.append("call alpine_miner_kmeans('").append(newTableName).append("','").append(temptablename).append("',");
				sql.append(columnsSize).append(", '").append(id).append("', ");
				sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
				sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
				sql.append(distance).append("").append(",?,?)");
				
				Object[] outputResult = new Object[2];
							
				try {
					if(!StringUtil.isEmpty(columnArrayList)){
						String columnsArray[]=columnArrayList.split(",");
						Array sqlArray = ((DBTable) dataSet.
								getDBTable()).getDatabaseConnection().getConnection().createArrayOf(
								"VARCHAR", columnsArray);
						CallableStatement stpCall = ((DBTable) dataSet.
								getDBTable()).getDatabaseConnection().getConnection().prepareCall(sql.toString());
						stpCall.setArray(1, sqlArray);
						stpCall.registerOutParameter(2, java.sql.Types.ARRAY);
						stpCall.execute();
						outputResult = (Object[]) stpCall.getArray(2).getArray();
						stpCall.close();
						
						double runtemp=(Double)outputResult[0];
						int run=(int)runtemp;
						bestModel.setMeasureAvg((Double)outputResult[1]);
						itsLogger.debug("Iteration times:" + String.valueOf(run));
						if (run == maxOptimizationSteps) {
							bestModel.setIsStable(false);
							bestModel
									.setStableInformation(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.KMEANS_NOTSTABLE, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
						}
					}
				} catch (SQLException e) {
					try {
						kmeansdb.dropTemp(st, temptablename, this.copyTableName);
						st.close();
					} catch (SQLException e1) {
						itsLogger.error(e1.getMessage(),e1);
					}
					if(e.getErrorCode()==-964){
						throw new WrongUsedException(this, AlpineAnalysisErrorName.DB2_LOG_TOO_LARGE);
					}else{
						throw e;
					}
				}
				
			}else if(dataSourceInfo.getDBType().equals(DataSourceInfoNZ.dBType)){
				String tempTableName="temp"+System.currentTimeMillis();
				StringBuilder createTempSql=new StringBuilder();
				createTempSql.append("create temp table ").append(tempTableName);
				createTempSql.append(" (id int,value varchar(1024)) ");
				itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+createTempSql.toString());
				st.execute(createTempSql.toString());
				
				int count=1;
				Columns columns = dataSet.getColumns();
				Iterator<Column> iter = columns.iterator();
				while(iter.hasNext()){
					Column column = iter.next();
					StringBuilder insertTableSql=new StringBuilder();
					insertTableSql.append("insert into ").append(tempTableName).append(" values ");
					insertTableSql.append(" (").append(count++).append(",'").append(column.getName()).append("')");
					itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+insertTableSql.toString());
					st.execute(insertTableSql.toString());
				}
				
				StringBuilder sql=new StringBuilder("");
				sql.append("call alpine_miner_kmeans('").append(newTableName).append("','").append(temptablename).append("','");
				sql.append(tempTableName).append("',");
				sql.append(columnsSize).append(", '").append(id).append("', ");
				sql.append("'").append(tempid).append("','").append(clusterColumnName).append("',");
				sql.append(k).append(",").append(maxRuns).append(" ,").append(maxOptimizationSteps).append(",");
				sql.append(distance).append(")");
				
				itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+sql);

				rs = st.executeQuery(sql.toString());
				
				while(rs.next()){
					String result=rs.getString(1);
					String[] results=result.split("_", 2);
					bestModel.setMeasureAvg(Double.parseDouble(results[1]));
					int run=Integer.parseInt(results[0]);
					itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():run="+run);
					if (run == maxOptimizationSteps) {
						bestModel.setIsStable(false);
						bestModel
								.setStableInformation(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.KMEANS_NOTSTABLE, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
					}
				}
				
			}else{
				String sql=kmeansdb.generateFunction(dataSet, maxColumnFlag, lineThrethhold, k, 
						maxOptimizationSteps, maxRuns, newTableName, 
						temptablename, id, tempid, clusterColumnName, columnNameList, columnArrayList, columnsSize, distance);

				itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+sql);

				rs = st.executeQuery(sql);

				while (rs.next()) {
					List<Double> b = kmeansdb.getArrayResult(rs, 1);
					int run=b.get(0).intValue();
					bestModel.setMeasureAvg(b.get(1));
					itsLogger.debug("Iteration times:" + String.valueOf(run));
					if (run == maxOptimizationSteps) {
						bestModel.setIsStable(false);
						bestModel
								.setStableInformation(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.KMEANS_NOTSTABLE, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
					}
				}
			}


			
			StringBuilder sb_map=new StringBuilder("select count(*), ");
			sb_map.append(clusterColumnName).append(" from ");
			sb_map.append(temptablename).append("table_name_temp  group by ").append(clusterColumnName); 
			sb_map.append(" order by 1");
			
			itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+sb_map.toString());
			rs=st.executeQuery(sb_map.toString());
			
			int count=0;
			while(rs.next())
			{
				clusterNameList.add(String.valueOf(count));
				bestModel.setCluster(new Cluster(String.valueOf(count), rs
						.getLong(1)));
				clustermap.put(String.valueOf(count), rs.getLong(1));
				clusterExchangeMap_oldKey.put(rs.getString(2), String.valueOf(count));
				count++;
			}
			rs.close();
			if(count<k){
				bestModel.setStableInformation(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.KMEANS_LESS_K, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
			}
			if (para.getResultSchema() != null
					&& para.getResultTableName() != null) {
				resulttableName = StringHandler.doubleQ(para.getResultSchema())
						+ "." + StringHandler.doubleQ(para.getResultTableName());
			}
			StringBuilder sb_create;
			if (para.getDropIfExist().equalsIgnoreCase(
					"yes")) {
				String dropTable=kmeansdb.dropTableIfExists(para.getResultSchema(),
						para.getResultTableName());
				itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+dropTable);
				st.execute(dropTable);
			} 
			
			DatabaseUtil.alterParallel(databaseConnection, "TABLE");
			boolean appendOnly = false;
			AnalysisStorageParameterModel analysisStorageParameterModel = para.getAnalysisStorageParameterModel();
			if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
				appendOnly = false;
			}else{
				appendOnly = true;
			}

			sb_create = new StringBuilder("create table ");
			StringBuilder selectSql=new StringBuilder();
			sb_create.append(resulttableName);
			sb_create.append(appendOnly ? sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel()) : " ");
			sb_create.append(DatabaseUtil.addParallel(databaseConnection, "TABLE")).append(" as (");
			selectSql.append(" select ");
			Iterator<Column> i = atts.iterator();
			while (i.hasNext()) {
				Column att = i.next();
				String columnName = StringHandler.doubleQ(att.getName());
				selectSql.append(columnName).append(",");
			}
			if(id!=null)
			{
				selectSql.append(id).append(",case ");	
			}else
			{
				id=tempid;
				selectSql.append("case ");
			}
			Iterator<String> i_Map=clusterExchangeMap_oldKey.keySet().iterator();
			while(i_Map.hasNext())
			{
				String clusterName=i_Map.next();
				selectSql.append(" when ").append(clusterColumnName).append("=").
				append(clusterName).append(" then ").append(clusterExchangeMap_oldKey.get(clusterName));
			}
			selectSql.append(" end ").append(clusterColumnName).append(" from ").append(temptablename).append(
					"table_name_temp").append(" x, ").append(copyTableName);
			selectSql.append(" y ").append(" where x.temp_id=y.").append(id);
			sb_create.append(selectSql).append(" ) ").append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()));
	
			itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+sb_create);
			st.execute(sb_create.toString());
			StringBuilder insertSql=new StringBuilder();
			insertSql.append(sqlGenerator.insertTable(selectSql.toString(), resulttableName));
			if(insertSql.length()>0){
				itsLogger.debug("KMeansDBFunctionPara.generateCenterPoint():sql="+insertSql);
				st.execute(insertSql.toString());
			}
			st.close();
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			String errormessage = e.getMessage();
			if(e.getSQLState() == null){
				throw new OperatorException(errormessage);
			}
			if (e.getSQLState().equals("2201E")) {
				String errormessageArray[] = errormessage.split("\\(");
				String ErrorString = errormessageArray[0].trim();
				if (ErrorString
						.equals("ERROR: cannot take logarithm of a negative number")) {
					itsLogger.debug("Throw user error 946");
					throw new WrongUsedException(this, AlpineAnalysisErrorName.LOG_NEGATIVE);
				}
				if(ErrorString.equals("ERROR: cannot take logarithm of zero"))
				{
					itsLogger.debug("Throw user error 951");
					throw new WrongUsedException(this, AlpineAnalysisErrorName.LOG_ZERO);
				}
			} else if (e.getSQLState().equals("22012")||e.getMessage().startsWith("ERROR:  Divide by 0")) {
				itsLogger.debug("Throw user error 947");
				throw new WrongUsedException(this, AlpineAnalysisErrorName.DEVIDE_BY_ZERO,para.getDistance());
			} else if (e.getSQLState().equals("22003")) {
				itsLogger.debug("Throw user error 948");
				throw new WrongUsedException(this, AlpineAnalysisErrorName.NUM_OUT_OF_RANGE,para.getDistance());
			} else if(e.getMessage().startsWith("ERROR:  can't take log of zero")){
				itsLogger.debug("Throw user error 951");
				throw new WrongUsedException(this, AlpineAnalysisErrorName.LOG_ZERO);
			} else if(errormessage.startsWith("ORA-06502: PL/SQL: numeric or value error:")){
				itsLogger.debug("Throw user error 951");
				throw new WrongUsedException(this, AlpineAnalysisErrorName.KMEANS_ORACLE10G_SQL_TOO_LONG);
			}
			else
			{
				throw new OperatorException(e.getLocalizedMessage());
			}
			
		}
		
		return resulttableName;
	}

	private void isDistinct(DatabaseConnection databaseConnection, String id, String tableName) throws OperatorException {
		try {
			Statement st = databaseConnection.createStatement(false);
			String sql = "select count(" + id+ "),count(distinct " + id
					+ ") from "
					+ tableName;
			itsLogger.debug("KMeansDBFunctionPara.isDistinct():sql="+sql);
			ResultSet rs = st.executeQuery(sql);
			rs.next();
			if (rs.getLong(1)!=rs.getLong(2)) {
				itsLogger.error("ID column Must be distinct");
				throw new WrongUsedException(this, AlpineAnalysisErrorName.ID_NOT_DISTINCT);
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

	}
	
}
