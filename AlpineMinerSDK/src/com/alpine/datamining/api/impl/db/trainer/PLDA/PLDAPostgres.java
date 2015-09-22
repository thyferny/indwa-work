/**
* ClassName PLDAPostgres.java
*
* Version information: 1.00
*
* Data: 2012-2-6
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.trainer.PLDA;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class PLDAPostgres extends PLDAImpl{
    private static final Logger itsLogger = Logger.getLogger(PLDAPostgres.class);

    @Override
	public void pldaTrain(Connection conncetion, Statement st,
			String dicSchema, String dicTable, String dicIndexColumn,
			String dicContentColumn, String contentSchema, String contentTable,
			String contentColumn, String contentIDColumn, long timeStamp,
			String dropIfExist, double alpha, double beta,
			String modelOutSchema, String modelOutTable, long topicnumber,
			long iterationNumber,String topicOutSchema, String topicOutTable, String topicDropIfExist
			,String docTopicTableDropIfExists,String docTopicOutSchema,String docTopicOutTable)
			throws SQLException {
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDAPostgres.pldaTrain():sql=Drop table IF EXISTS " + StringHandler.doubleQ(modelOutSchema) + "." +
                        StringHandler.doubleQ(modelOutTable));
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(modelOutSchema)+"."+
						StringHandler.doubleQ(modelOutTable));
			}
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDAPostgres.pldaTrain():sql=Drop table IF EXISTS " + StringHandler.doubleQ(docTopicOutSchema) + "." +
                        StringHandler.doubleQ(docTopicOutTable));
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(docTopicOutSchema)+"."+
					StringHandler.doubleQ(docTopicOutTable));
			}
			if (topicDropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDAPostgres.pldaTrain():sql=Drop table IF EXISTS " + StringHandler.doubleQ(topicOutSchema) + "." +
                        StringHandler.doubleQ(topicOutTable));
				st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(topicOutSchema)+"."+StringHandler.doubleQ(topicOutTable));
			}
				
				
		StringBuffer sql = new StringBuffer();
		sql.append("select ")
		.append(AlpineStoredProcedure.PLDA_TRAIN_STRING)
		.append("('")
		.append(StringHandler.doubleQ(contentSchema)).append(".").append(StringHandler.doubleQ(contentTable))
		.append("','").append(StringHandler.doubleQ(contentIDColumn))
		.append("','").append(StringHandler.doubleQ(contentColumn))
		.append("',").append(alpha)
		.append(",").append(beta)
		.append(",").append(topicnumber)
		.append(",'").append(StringHandler.doubleQ(dicSchema)).append(".").append(StringHandler.doubleQ(dicTable))
		.append("','").append(StringHandler.doubleQ(dicContentColumn))
		.append("',").append(iterationNumber)
		.append(",'").append(StringHandler.doubleQ(PLDAPreString+timeStamp))
		.append("','").append(StringHandler.doubleQ(PLDAPreString+timeStamp+PLDANoArrayPreString))
		.append("','").append(StringHandler.doubleQ(modelOutSchema)).append(".").append(StringHandler.doubleQ(modelOutTable))
		.append("','").append(StringHandler.doubleQ(topicOutSchema)).append(".").append(StringHandler.doubleQ(topicOutTable))
		.append("','").append(StringHandler.doubleQ(docTopicOutSchema)).append(".").append(StringHandler.doubleQ(docTopicOutTable))
		.append("')");
		itsLogger.debug("PLDAPostGres.pldaTrain():sql=" + sql.toString());

		st.executeQuery(sql.toString());
		
	}





}
