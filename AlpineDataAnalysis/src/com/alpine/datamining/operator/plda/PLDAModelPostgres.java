/**
* ClassName PLDAModelPostgres.java
*
* Version information: 1.00
*
* Data: 2012-2-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.operator.plda;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class PLDAModelPostgres extends PLDAModel{
    private static Logger itsLogger= Logger.getLogger(PLDAModelPostgres.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = -4404507960517382916L;

	/**
	 * @param trainingDataSet
	 */
	public PLDAModelPostgres(DataSet trainingDataSet) {
		super(trainingDataSet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void PLDAPredict(String predictTable, long iterationNumber,
			String docOutSchema, String docOutTable, String appendOnlyString, String endingString, String topicOutSchema,
			String topicOutTable, String docTopicAppendOnlyString, String docTopicEndingString,  Connection conncetion, String dropIfExist,
			String dropIfExistRTable) throws SQLException {
		Statement st=null;
		
		try {
			
			st = conncetion.createStatement();
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDAModelDB2.pldaPredict():sql=Drop table IF EXISTS" + StringHandler.doubleQ(docOutSchema) + "." + StringHandler.doubleQ(docOutTable));
				
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(docOutSchema)+"."+StringHandler.doubleQ(docOutTable));
			}
			if (dropIfExistRTable.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDAModelDB2.pldaPredict():sql=Drop table IF EXISTS" + StringHandler.doubleQ(topicOutSchema) + "." + StringHandler.doubleQ(topicOutTable));
				
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(topicOutSchema)+"."+StringHandler.doubleQ(topicOutTable));
			}
			long timeStamp = System.currentTimeMillis();
			StringBuffer sql=new StringBuffer();
			sql.append("select ")
			.append(AlpineStoredProcedure.PLDA_PREDICT_STRING)
			.append("('")
			.append(StringHandler.doubleQ(this.getModelSchema()))
			.append(".")
			.append(StringHandler.doubleQ(this.getModelTable()))
			.append("','").append(predictTable)
			.append("','").append(StringHandler.doubleQ(this.getDocIdColumn()))
			.append("','").append(StringHandler.doubleQ(this.getDocContentColumn()))
			.append("',").append(this.getAlpha())
			.append(",").append(this.getBeta())
			.append(",").append(this.getTopicNumber())
			.append(",").append(iterationNumber)
			.append(",'")
			.append(StringHandler.doubleQ(this.getDictSchema()))
			.append(".").append(this.getDictTable())
			.append("','").append(StringHandler.doubleQ(this.getDicContentColumn()))
			.append("','").append(StringHandler.doubleQ(PLDAPreString+timeStamp))
			.append("','").append(StringHandler.doubleQ(docOutSchema)).append(".").append(StringHandler.doubleQ(docOutTable))
			.append("','").append(StringHandler.doubleQ(topicOutSchema)).append(".").append(StringHandler.doubleQ(topicOutTable))
			.append("')");
			itsLogger.debug("PLDAModelPostGres.PLDAPredict():sql=" + sql.toString());

			st.executeQuery(sql.toString());

			
			
		} catch (SQLException e) {
			itsLogger.debug("PLDAModelPostGres.PLDAPredict()" + e);
			throw new SQLException(e);
			
		}finally{
			try {
				if(st != null)
					{
						st.close();
					} 
				}catch (SQLException e) {
					itsLogger.debug(e.toString());
					throw  e;
					}

		}
		
	}

}
