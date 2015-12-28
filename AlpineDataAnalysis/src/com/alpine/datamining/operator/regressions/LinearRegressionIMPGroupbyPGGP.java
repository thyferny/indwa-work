
package com.alpine.datamining.operator.regressions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.ColumnTypeInteractionTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class LinearRegressionIMPGroupbyPGGP extends LinearRegressionImpPGGP{
    private static final Logger itsLogger = Logger.getLogger(LinearRegressionIMPGroupbyPGGP.class);

    protected String groupbyColumn = "";
	protected HashMap<String,ArrayList<String>> null_list_group=new HashMap<String,ArrayList<String>> ();
	protected HashMap<String,Double []> coefficients = new HashMap<String,Double []>();
	HashMap<String,HashMap<String,Double>> coefficientmap=new HashMap<String,HashMap<String,Double>>();
	protected LinearRegressionGroupGPModel model = null;
	DataSet dataSet;
	DataSet newDataSet = null;
	ColumnTypeInteractionTransformer transformer =new ColumnTypeInteractionTransformer();
	protected HashMap<String,Matrix> hessian = new HashMap<String,Matrix>();
	protected HashMap<String,Integer> groupCount =new HashMap<String,Integer>();
	protected String classLogInfo=LinearRegressionIMPGroupbyPGGP.class.getCanonicalName();
	protected List<String> dataErrorList=new ArrayList<String>();
	
	public String getGroupbyColumn() {
		return groupbyColumn;
	}

	public void setGroupbyColumn(String groupbyColumn) {
		this.groupbyColumn = groupbyColumn;
	}
	
	
	public Model learn(DataSet dataSet, LinearRegressionParameter para, String columnNames) throws OperatorException {
		this.dataSet = dataSet;
		ArrayList<String> columnNamesList = new ArrayList<String>();
		if (columnNames != null && !StringUtil.isEmpty(columnNames.trim())){
			String[] columnNamesArray=columnNames.split(",");
			for(String s:columnNamesArray)
			{
				columnNamesList.add(s);
			}
		}
		transformer.setColumnNames(columnNamesList);
		transformer.setAnalysisInterActionModel(para.getAnalysisInterActionModel());
		newDataSet=transformer.TransformCategoryToNumeric_new(dataSet,groupbyColumn);
		DatabaseConnection databaseConnection = ((DBTable) newDataSet
				.getDBTable()).getDatabaseConnection();
		
		Column label =newDataSet.getColumns().getLabel();
		String labelName =StringHandler.doubleQ(label.getName()) ;
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();

		String newTableName=((DBTable) newDataSet.getDBTable())
		.getTableName();
		
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		try {
			newDataSet.computeAllColumnStatistics();
			Columns atts=newDataSet.getColumns();

			Iterator<Column> atts_i=atts.iterator();
		
			int count=0;
			String [] columnNamesArray = new String[atts.size()];
			while(atts_i.hasNext())
			{
				Column att=atts_i.next();
				columnNamesArray[count]=att.getName();
				count++;
			}
			null_list=calculateNull(dataSet);
			null_list_group=calculateNullGroup(newDataSet,atts);
			StringBuilder sb_notNull = getWhere(atts);

			getCoefficientAndR2Group(columnNames, dataSet, labelName,tableName,
					newTableName, atts, columnNamesArray, sb_notNull);

			HashMap<String,Long> degreeOfFreedom = new HashMap<String,Long>();
			for(String groupValue:groupCount.keySet())
			{
				long tempDof=	groupCount.get(groupValue) - columnNamesArray.length - 1;
				if(tempDof<=0)
				{
					model.getOneModel(groupValue).setS(Double.NaN); 
				}
				degreeOfFreedom.put(groupValue, tempDof);
			}
			
//			if (dof <= 0)
//			{
//				model.setS(Double.NaN);
//				return model;
//			}
			StringBuffer sSQL = createSSQLLGroup(newDataSet, newTableName, label,
					columnNamesArray, coefficients,sb_notNull);
			HashMap<String, Double> sValueMap = new HashMap<String,Double>();

			try {
				itsLogger.debug(classLogInfo+".learn():sql="+sSQL);
				rs = st.executeQuery(sSQL.toString());			
				while (rs.next()) {
					String groupValue=rs.getString(2);
					if(groupValue == null)
					{
						continue;
					}
					if(dataErrorList.contains(groupValue))
					{
						sValueMap.put(groupValue, Double.NaN);
					}else
					{
						sValueMap.put(groupValue,  rs.getDouble(1));
					}
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}

			HashMap<String,Matrix> varianceCovarianceMatrix = getVarianceCovarianceMatrixGroup(
					newTableName, columnNamesArray,  st);
			for (String groupValue:varianceCovarianceMatrix.keySet())
			{
				if(varianceCovarianceMatrix==null)
				{
					model.getOneModel(groupValue).setErrorString(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.MATRIX_IS_SIGULAR, AlpineThreadLocal.getLocale())+Tools.getLineSeparator());
				}
			}
			caculateStatistics( columnNamesArray, coefficients, model, sValueMap,
					varianceCovarianceMatrix, degreeOfFreedom);
			for (String groupValue:null_list_group.keySet())
			{
				if(null_list_group.get(groupValue).size()!=0)
				{
				StringBuilder sb_null=new StringBuilder();
				for(int i=0;i<null_list_group.get(groupValue).size();i++)
				{
					sb_null.append(StringHandler.doubleQ(null_list_group.get(groupValue).get(i))).append(",");
				}
				sb_null=sb_null.deleteCharAt(sb_null.length()-1);
				String table_exist_null=AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.TABLE_EXIST_NULL, AlpineThreadLocal.getLocale());
				String[] temp=table_exist_null.split(";");
				model.getOneModel(groupValue).setErrorString(temp[0]+sb_null.toString()+temp[1]+Tools.getLineSeparator());
				}
			}
			if(transformer.isTransform())
			{
				dropTable(newTableName);
			}
			st.close();
            itsLogger.debug(LogUtils.exit(classLogInfo, "learn", model.toString()));
			model.setGroupByColumn(groupbyColumn);
			return model;
			
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	
	
	
	
	
	
	
	
	protected HashMap<String, ArrayList<String>> calculateNullGroup(DataSet dataSet, Columns atts) throws OperatorException
	{
		
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		HashMap<String,ArrayList<String>> resultList=new HashMap<String,ArrayList<String>>();
		Iterator<Column> i=dataSet.getColumns().iterator();
		StringBuilder sb_count=new StringBuilder("select ");
		sb_count.append(StringHandler.doubleQ(groupbyColumn)).append(", count(*) ");

		sb_count.append(" from ").append(tableName).append(this.getWhere(atts)).append(" group by ").append(StringHandler.doubleQ(groupbyColumn));
		try {
			Statement st=databaseConnection.createStatement(false);
			itsLogger.debug("LinearRegressionImp.calculateNull():sql="
                    + sb_count.toString());

			ResultSet rs=st.executeQuery(sb_count.toString());
			while(rs.next())
			{
				String groupValue="";
				groupValue=rs.getString(1);
				this.groupCount.put(groupValue, rs.getInt(2));
			}
		}catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		sb_count=new StringBuilder("select ").append(StringHandler.doubleQ(groupbyColumn)).append(", ");
		while(i.hasNext())
		{
			Column att=i.next();
			sb_count.append("count(").append(StringHandler.doubleQ(att.getName())).append(")");
			sb_count.append(StringHandler.doubleQ(att.getName())).append(",");
		}
		sb_count=sb_count.deleteCharAt(sb_count.length()-1);
		sb_count.append(" from ").append(tableName).append(this.getWhere(atts)).append(" group by ").append(StringHandler.doubleQ(groupbyColumn));
		try {
			Statement st=databaseConnection.createStatement(false);
			itsLogger.debug("LinearRegressionImp.calculateNull():sql="
                    + sb_count.toString());

			ResultSet rs=st.executeQuery(sb_count.toString());
			while(rs.next())
			{
				ArrayList<String> null_list=new ArrayList<String>();
				String groupValue="";
				groupValue=rs.getString(1);
				for(int j=1;j<rs.getMetaData().getColumnCount();j++)
				{
					if(rs.getFloat(j+1)!=groupCount.get(groupValue))
					{
						null_list.add(dataSet.getColumns().get(rs.getMetaData().getColumnName(j+1)).getName());
					}
				}
				resultList.put(groupValue, null_list);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return resultList;
	}
	
	protected StringBuffer createSSQLLGroup(DataSet dataSet, String tableName,
			Column label, String[] columnNames, HashMap<String, Double[]> coefficients2,StringBuilder sb_notNull) {
		StringBuffer predictedY = new StringBuffer("( case ");
		StringBuffer countString = new StringBuffer(" (case ");
		for(String groupValue : coefficients2.keySet())
		{
			predictedY.append(" when ").append(StringHandler.doubleQ(groupbyColumn)).append("=").append(StringHandler.singleQ(groupValue)).append(" then ").append(coefficients.get(groupValue)[coefficients.get(groupValue).length - 1]);
			for (int i = 0; i < columnNames.length; i++){
				predictedY.append("+").append(coefficients2.get(groupValue)[i]).append("*\"").append(columnNames[i]).append("\"");
			}
			if(groupCount.get(groupValue)>(columnNames.length + 1))
				{
					countString.append(" when ").append(StringHandler.doubleQ(groupbyColumn)).append("=").append(StringHandler.singleQ(groupValue)).append(" then ").append(groupCount.get(groupValue)).append(" - ").append(columnNames.length + 1);
				}
				else 
				{
					countString.append(" when ").append(StringHandler.doubleQ(groupbyColumn)).append("=").append(StringHandler.singleQ(groupValue)).append(" then ").append(" null ");
					dataErrorList.add(groupValue);
				}
		}
		countString.append(" end )::double precision"); 
		predictedY.append(" end ");
		String labelName=StringHandler.doubleQ(label.getName());
		predictedY.append(")");
		StringBuffer sSQL = new StringBuffer("select sqrt(");
			
		sSQL.append("sum((").append(labelName).append(" - ").append(predictedY).append(")*1.0*(").append(labelName).append(" - ").append(predictedY).append("))/")
			.append("( ").append(countString).append(")");
		sSQL.append("),").append(StringHandler.doubleQ(groupbyColumn)).append(" from ").append(tableName).append(" ").append(sb_notNull).append(" group by ").append(StringHandler.doubleQ(groupbyColumn));
		return sSQL;
	}
	
	protected void getCoefficientAndR2Group(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder sb_notNull) throws OperatorException {
		Iterator<Column> atts_i;
		StringBuffer columnNamesArray = new StringBuffer();
		columnNamesArray.append("array[1.0,");
					
		atts_i=atts.iterator();
		int i = 0;
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			if(i != 0){
				columnNamesArray.append(",");
			}
			columnNamesArray.append(StringHandler.doubleQ(att.getName())).append("::float");
			i++;
		}
		columnNamesArray.append("]");
		String sql = null;
		sql = "select alpine_miner_mregr_coef("+labelName+"::float,"+columnNamesArray+") , "+ StringHandler.doubleQ(groupbyColumn)
		+" from "+newTableName+sb_notNull+ "     group by "+StringHandler.doubleQ(groupbyColumn);
		itsLogger.debug(classLogInfo+".getCoefficientAndR2():sql="+sql);
		HashMap<String,Matrix> XY=new HashMap<String,Matrix>();
				      	try {
      		Object[] object = null;
			ResultSet rs = st.executeQuery(sql.toString());
			while(rs.next())
			{
				Matrix tempXY = new Matrix(columnNames.length + 1,1);

				Matrix tempHessian = new Matrix(columnNames.length + 1, columnNames.length + 1);
				String groupValue=rs.getString(2);
				object=(Object[])rs.getArray(1).getArray();
				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					int y = x + 1;
					double doubleValue = 0.0;
					if (object[y] != null)
					{
						if (object[y] instanceof BigDecimal){
							doubleValue = ((BigDecimal)object[y]).doubleValue();
						}else if (object[y] instanceof Double){
							doubleValue = ((Double)object[y]).doubleValue();
						}else if (object[y] instanceof Integer){
							doubleValue = ((Integer)object[y]).doubleValue();
						}else{
							doubleValue = ((Number)object[y]).doubleValue();
						}
					}
					tempXY.set(x, 0, doubleValue);
				}
				XY.put(groupValue, tempXY);
				double [] arrayarrayResult = getHessian(object, columnNames.length + 2, (columnNames.length + 1)*(columnNames.length + 2)/2);//new double[sbAllArray.size()];
				i=0;

				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					for (int y = x; y < columnNames.length + 1; y++) {
						{
							double h = 0.0;
							if(!Double.isNaN(arrayarrayResult[i])){
								h=arrayarrayResult[i];
							}
							tempHessian.set(x, y, h);
							if(x!=y)
							{
								tempHessian.set(y, x, h);
							}
							i++;
						}
					}
				}
				hessian.put(groupValue, tempHessian);
				
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
			boolean first = true;
		for(String tempString:hessian.keySet())
		{
			Matrix beta = null;
		   	Matrix varianceCovarianceMatrix = null;
		   	Double [] tempCoefficients = new Double[columnNames.length + 1];
		   	for(i = 0; i < tempCoefficients.length; i++){
		   		tempCoefficients[i] = 0.0;
		   	}

		   	try {
		   		varianceCovarianceMatrix = hessian.get(tempString).SVDInverse();
		   		beta = varianceCovarianceMatrix.times(XY.get(tempString));

    		for (i = 0; i < beta.getRowDimension(); i++)
    		{
    			if (i == 0)
    			{
    				tempCoefficients[beta.getRowDimension() - 1] = beta.get(i, 0);
    			}
    			else
    			{
    				tempCoefficients[i - 1] = beta.get(i, 0);
    			}
    		}
    		coefficients.put(tempString, tempCoefficients);
    	
    		double r2 = 0;
    		getGroupCoefficientMap(columnNames, tempString);
    		if(first== true)
    		{
    			model =  new LinearRegressionGroupGPModel(dataSet,columnNames, columNames, tempCoefficients,coefficientmap.get(tempString));
        		first=false;
    		}
    		LinearRegressionModelDB tempModel =  new LinearRegressionModelDB(dataSet,columnNames, columNames, tempCoefficients,coefficientmap.get(tempString));
			if(!this.newDataSet.equals(this.dataSet))
			{
				tempModel.setAllTransformMap_valueKey(transformer.getAllTransformMap_valueKey());
			}
			tempModel.setInteractionColumnExpMap(transformer.getInteractionColumnExpMap());
			tempModel.setInteractionColumnColumnMap(transformer.getInteractionColumnColumnMap());
			r2 = cacluateGroupRSquare(dataSet, tableName,
				labelName, tempModel,tempString,sb_notNull);			
			tempModel.setR2(r2);
			model.addOneModel(tempModel, tempString);
		   	} catch (Exception e) {
    		itsLogger.error(e.getMessage(),e);
    		}
		}
	}

	protected void getGroupCoefficientMap(String[] columnNames,String groupValue) {
		HashMap<String,Double> tempCoefficientmap= new HashMap<String,Double>();
		for(int i=0;i<coefficients.get(groupValue).length;i++)
		{
			if(i==0)
			{
				tempCoefficientmap.put("intercept", coefficients.get(groupValue)[coefficients.get(groupValue).length-1]);
			}
			else
			{
				tempCoefficientmap.put(columnNames[i-1], coefficients.get(groupValue)[i-1]);
			}
			coefficientmap.put(groupValue, tempCoefficientmap);		
		}
	}	
	
	
	protected void caculateStatistics(String[] columnNames, HashMap<String, Double[]> coefficients2,
			LinearRegressionGroupGPModel model, HashMap<String, Double> s,
			HashMap<String, Matrix> varianceCovarianceMatrix,HashMap<String, Long> dof) {
		for (String tempString:model.getModelList().keySet())
		{
			double[] se = new double[columnNames.length + 1];
		   	double [] t = new double[columnNames.length + 1];
		   	double [] p = new double[columnNames.length + 1];

//    	int newI = 0;
		   	for (int i = 0; i < columnNames.length; i++) {
		   		if(varianceCovarianceMatrix!=null)
		   		{
		   			if (Double.isNaN(varianceCovarianceMatrix.get(tempString).get(i+1, i+1)))
		   			{
		   				se[i] = Double.NaN;
		   			}
		   			else
		   			{
		   				se[i] = s.get(tempString)*Math.sqrt(Math.abs(varianceCovarianceMatrix.get(tempString).get(i+1, i+1)));
		   			}
		   		}else
		   		{
		   			se[i] = Double.NaN;
		   		}
		   		t[i] = coefficients2.get(tempString)[i]/se[i];
		   		p[i] = studT(t[i],dof.get(tempString));
		   	}
		   	if(varianceCovarianceMatrix != null)
		   	{
		   		if (Double.isNaN(varianceCovarianceMatrix.get(tempString).get(0, 0)))
		   		{
		   			se[columnNames.length] = Double.NaN;
		   		}
		   		else
		   		{
		   			se[columnNames.length] = s.get(tempString)*Math.sqrt(Math.abs(varianceCovarianceMatrix.get(tempString).get(0, 0)));
		   		}
		   	}else
		   	{
		   		se[columnNames.length] = Double.NaN;
		   	}
		   	t[columnNames.length] = coefficients2.get(tempString)[columnNames.length]/se[columnNames.length];
		   	p[columnNames.length] = studT(t[columnNames.length],dof.get(tempString));;

		   	model.getOneModel(tempString).setS(s.get(tempString));
		   	model.getOneModel(tempString).setSe(se);
		   	model.getOneModel(tempString).setT(t);
		   	model.getOneModel(tempString).setP(p);
		}
	}
	
	
	protected HashMap<String,Matrix> getVarianceCovarianceMatrixGroup(String tableName,
			String[] columnNames, Statement st) throws OperatorException {
		HashMap<String,Matrix> varianceCovarianceMatrix = new HashMap<String,Matrix>();
    	try {
    		for(String groupValue:hessian.keySet())
    		
    		varianceCovarianceMatrix.put(groupValue,  hessian.get(groupValue).SVDInverse());
    	} catch (Exception e) {
    			return null;
    	}
		return varianceCovarianceMatrix;
	}
	
	
	
	protected StringBuilder getWhere(Columns atts) {
//		if (null_list_group.size() == 0){
//			return new StringBuilder("");
//		}
		Iterator<Column> atts_i;
		StringBuilder sb_notNull=new StringBuilder(" where ");
		atts_i=atts.iterator();
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
//			if(null_list.get(att).contains(att.getName()))
//			{
				sb_notNull.append(StringHandler.doubleQ(att.getName())).append(" is not null and ");
//			}
		}
		sb_notNull.append(StringHandler.doubleQ(this.groupbyColumn)).append(" is not null and ");
		sb_notNull.delete(sb_notNull.length()-4, sb_notNull.length());
		return sb_notNull;
	}
	
	
	protected double cacluateGroupRSquare(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model,String groupValue,StringBuilder sb_notNull) throws OperatorException {
//		String notnull=sb_notNull.toString().substring(beginIndex, endIndex);
//		notnull.replace("where", " and ");
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(").append(labelName).append(")  from ").append(tableName).append(sb_notNull).append(" and ").append(StringHandler.doubleQ(groupbyColumn)).append("=").append(StringHandler.singleQ(groupValue));
		double avg = 0.0;
		try {
			itsLogger.debug(classLogInfo+".cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				avg = rs.getDouble(1);
			}	
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			return Double.NaN;
		}

		StringBuffer predictedValueSQL = new StringBuffer();
		predictedValueSQL.append(model.generatePredictedString(dataSet));
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-").append(labelName).
		append(")*(").append(predictedValueSQL).append("-").append(labelName).
		append("))*1.0/sum((").append(labelName).append("-(").append(avg).append("))*(").append(labelName).append("-(").append(avg).append("))) from ").append(tableName).append(sb_notNull).append(" and ").append(StringHandler.doubleQ(groupbyColumn)).append("=").append(StringHandler.singleQ(groupValue)) ;
		try {
			itsLogger.debug("LinearRegressionImpPGGP.cacluateRSquare():sql="+RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				RSquare = rs.getDouble(1);
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			return Double.NaN;
		}
		return RSquare;
	}
	
}
	

