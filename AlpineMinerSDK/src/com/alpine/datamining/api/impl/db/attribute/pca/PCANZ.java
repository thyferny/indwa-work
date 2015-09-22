/**
* ClassName PCANZ.java
*
* Version information: 1.00
*
* Data: 20 Dec 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.attribute.pca;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class PCANZ extends PCAImpl{

    private static Logger itsLogger = Logger.getLogger(PCANZ.class);

    private String pcaInitC="pcainitc";
	private String pcaRemain="pcaRemain";
	private String columnNameTable=null;
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.pca.PCAImpl#checkErr(java.lang.String, double)
	 */
	@Override
	public boolean ValidateConstant(String anaType, double tempNumber) {		
		if (anaType.equalsIgnoreCase(PCACovPop)
				|| anaType.equalsIgnoreCase(PCACovSam)) {
			if (tempNumber == 0)
				return true;
		} else if (anaType.equalsIgnoreCase(PCACorr)) {
			if (Double.compare(tempNumber,Double.NaN) == 0)
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.pca.PCAImpl#getPCAResult(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.sql.Statement, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.String, int, com.alpine.datamining.utility.DatabaseConnection)
	 */
	@Override
	public void generatePCAResult(String remainColumns, String[] remainColumnsArray,
			String tableName, String DBType, String outSchema, String outTable,
			String valueOutTable, String valueOutSchema, int remainNumber,
			Statement st, StringBuffer columnArray, StringBuffer remainArray,
			String dropIfExists, int PCANumber,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException {
		long timeStamp = System.currentTimeMillis();
		String remainNameTable=pcaRemain+timeStamp;
		
		String sql="create table "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(remainNameTable)
		+ "	(  remaincolumninfo varchar("+DataSourceInfoNZ.maxColumnLength+"))";
		itsLogger.debug("PCANZ.initPCA():sql=" +sql);
		 st.execute(sql);
		
		
		if(!StringUtil.isEmpty(remainColumns)){
					
			for (int i = 0; i < remainNumber; i++) {
				sql="insert into "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(remainNameTable)
				+ " values ('" +  StringHandler.doubleQ(StringHandler.escQ(remainColumnsArray[i]))  + "')";
				itsLogger.debug("PCANZ.initPCA():sql=" +sql);
				 st.execute(sql);
				
			}
			
		}
		sql="select alpine_miner_pcaresult('"+tableName+ "','"
		+ StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(columnNameTable) + "','" + StringHandler.doubleQ(outSchema)+"."+StringHandler.doubleQ(outTable) + "','"+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(remainNameTable)+"','"+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(valueOutTable)+"',"
		+ PCANumber + ",'"+dropIfExists+"',"+AlpineDataAnalysisConfig.NZ_ALIAS_NUM+")";
		itsLogger.debug("PCANZ.getPCAResult():sql="+sql);
		st.executeQuery(sql);
		sql="drop table "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(remainNameTable);
		 itsLogger.debug("PCANZ.getPCAResult():sql=" +sql);
		 st.execute(sql);
		
		
		 sql="drop table "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(columnNameTable);
		 itsLogger.debug("PCANZ.getPCAResult():sql=" +sql);
		 st.execute(sql);
		
		
	
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.pca.PCAImpl#initPCA(java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.sql.Statement, java.lang.StringBuffer, java.sql.ResultSet, java.lang.String, com.alpine.datamining.utility.DatabaseConnection)
	 */
	@Override
	public Object[] initPCA(String[] columnsArray, String tableName,
			String anaType, String valueOutTable, String valueOutSchema,
			int columnsNumber, Statement st, StringBuffer columnArray,
			ResultSet rs, String dropIfExists,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException {
		long timeStamp = System.currentTimeMillis();
		 columnNameTable=pcaInitC+timeStamp;
		StringBuffer createSQL=new StringBuffer();
		double[] tempResult = new double[(1+columnsArray.length)*columnsArray.length/2+columnsArray.length];
		StringBuffer notnull=new StringBuffer();
		if(dropIfExists.equalsIgnoreCase("yes"))
		{
			String dropSQL="select droptable_if_existsdoubleq('"+ StringHandler.doubleQ(valueOutTable)+"')";
			itsLogger.debug("PCANZ.initPCA():sql=" +dropSQL);
			rs=st.executeQuery(dropSQL);
		}
		createSQL.append(" create table ");
		createSQL.append(StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(valueOutTable));
		createSQL.append(" ( ");
		notnull.append(" where ");
		long rownumber=0;
		StringBuffer getValueSQL=new StringBuffer();
		getValueSQL.append(" select ");
		int totalnumber=0;
		int tempnumber=0;
		int frequency=0;
		int maxColumnNumber=Integer.parseInt(AlpineDataAnalysisConfig.MAX_COLUMN);
		for (int i = 0; i < columnsNumber; i++) {
			for (int j = i; j < columnsNumber; j++){
				if(tempnumber>=maxColumnNumber)
				{
					frequency++;
					getValueSQL.deleteCharAt(getValueSQL.length()-1);
					getValueSQL.append(" from ").append(tableName);
					itsLogger.debug("PCANZ.initPCA():sql=" +getValueSQL);
					rs=st.executeQuery(getValueSQL.toString());
					while(rs.next())
					{
						for (int k = 0; k < tempnumber; k++)
						{
							tempResult[(frequency-1)*maxColumnNumber+k]=rs.getDouble(k+1);
						}
					}
					getValueSQL.setLength(0);
					getValueSQL.append(" select ");
					tempnumber=0;
				}
					
				
				getValueSQL.append(" sum(").append(StringHandler.doubleQ(columnsArray[i]))
						.append("::double*").append(StringHandler.doubleQ(columnsArray[j]))
						.append("::double) ,");
				totalnumber++;
				tempnumber++;
				
			}
		}
		getValueSQL.deleteCharAt(getValueSQL.length()-1);
		getValueSQL.append(" from ").append(tableName);
		itsLogger.debug("PCANZ.initPCA():sql=" +getValueSQL);
		rs=st.executeQuery(getValueSQL.toString());
		while(rs.next())
		{
			for (int k = 0; k < tempnumber; k++)
			{
				tempResult[frequency*maxColumnNumber+k]=rs.getDouble(k+1);
			}
		}
		int sumnumber=frequency*maxColumnNumber+tempnumber;
		getValueSQL.setLength(0);
		getValueSQL.append(" select ");
		tempnumber=0;
		for (int i = 0; i < columnsNumber; i++) {
			getValueSQL.append(" avg(").append(StringHandler.doubleQ(columnsArray[i])).append("::double) ,");
			
			notnull.append(StringHandler.doubleQ(columnsArray[i])).append(" is not null and");
			  createSQL.append(StringHandler.doubleQ(columnsArray[i])).append(" double ,");
		}
		
		createSQL.append(" \"alpine_pcadataindex\" integer,\"alpine_pcaevalue\" double,\"alpine_pcacumvl\" double,\"alpine_pcatotalcumvl\" double) ");
		itsLogger.debug("PCANZ.initPCA():sql=" +createSQL);
		st.execute(createSQL.toString());
		notnull.delete(notnull.length()-4, notnull.length());
		getValueSQL.deleteCharAt(getValueSQL.length()-1);
		getValueSQL.append(" from ").append(tableName);
		itsLogger.debug("PCANZ.initPCA():sql=" +getValueSQL);
		rs=st.executeQuery(getValueSQL.toString());
		while(rs.next())
		{
			for (int i = 0; i < columnsNumber; i++)
			{
				tempResult[sumnumber+i]=rs.getDouble(i+1);
			}
		}
		StringBuffer countSQL=new StringBuffer();
		countSQL.append("select count(*) from ").append(tableName).append(notnull);
		itsLogger.debug("PCANZ.initPCA():sql=" +countSQL);
		rs=st.executeQuery(countSQL.toString());
		while(rs.next())
		{
			rownumber=rs.getLong(1);
		}
		
		Object[] outputResult = new Object[(1+columnsArray.length)*columnsArray.length/2];
		
//		 int
		tempnumber=0;
		if( anaType.equalsIgnoreCase(PCACovSam))
		   {
			   for (int i = 0; i < columnsNumber; i++)
			   {   for (int j = i; j < columnsNumber; j++)
				   {
					   outputResult[tempnumber] = (tempResult[tempnumber] -
		                                    rownumber * tempResult[totalnumber + i] *
		                                    tempResult[totalnumber + j]) /
		                                    (rownumber - 1);
					   tempnumber++;
				   }
			   }
		   }
		   else if( anaType.equalsIgnoreCase(PCACovPop))
		   {   
			   for (int i = 0; i < columnsNumber; i++)
			   {
				   for (int j = i; j < columnsNumber; j++)
			   
				   {
					   outputResult[tempnumber] = (tempResult[tempnumber] -
                               rownumber * tempResult[totalnumber + i] *
                               tempResult[totalnumber + j]) /rownumber;
					   tempnumber++;
				   }
			   }
		   }
		   else if( anaType.equalsIgnoreCase(PCACorr))
		  {
			   for (int i = 0; i < columnsNumber; i++)
				{   for (int j = i; j < columnsNumber; j++)
				   {
					   outputResult[tempnumber]= (tempResult[tempnumber] -  rownumber * tempResult[totalnumber + i] *
							   						tempResult[totalnumber + j]) /
		                                        (Math.sqrt(tempResult[(2 * columnsArray.length   - i+1) *
		                                                     (i ) / 2 ] -
		                                              rownumber *
		                                              tempResult[totalnumber + i] *
		                                              tempResult[totalnumber + i]) *
		                                              Math.sqrt(tempResult[(2 * columnsArray.length   - j+1) *
		                                                     (j ) / 2 ] -
		                                              rownumber *
		                                              tempResult[totalnumber + j] *
		                                              tempResult[totalnumber + j]));
					   tempnumber++;
				   }
				}
		  }
		String sql="create table "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(columnNameTable)
		+ "	(  columninfo varchar("+DataSourceInfoNZ.maxColumnLength+") ,dataindex integer )";
		itsLogger.debug("PCANZ.initPCA():sql=" +sql);
		st.execute(sql);
		for (int i = 0; i < columnsNumber; i++) {
			sql="insert into "+StringHandler.doubleQ(valueOutSchema)+"."+StringHandler.doubleQ(columnNameTable)
			+ " values ('" + StringHandler.doubleQ(columnsArray[i]) + "',"+i+")";
			itsLogger.debug("PCANZ.initPCA():sql=" +sql);
			 st.execute(sql);
		
			}
	return outputResult;
		
	}

}
