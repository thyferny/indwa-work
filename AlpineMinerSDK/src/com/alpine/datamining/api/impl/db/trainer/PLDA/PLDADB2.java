/**
* ClassName PLDADB2.java
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
public class PLDADB2 extends PLDAImpl{
    private static final Logger itsLogger = Logger.getLogger(PLDADB2.class);

    Connection connection = null;
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}



	@Override
	public void pldaTrain(Connection conncetion, Statement st,
			String dicSchema, String dicTable, String dicIndexColumn,
			String dicContentColumn, String contentSchema, String contentTable,
			String contentColumn, String contentIDColumn, long timeStamp,
			String dropIfExist, double alpha, double beta,
			String modelOutSchema, String modelOutTable, long topicnumber,
			long iterationNumber, String topicOutSchema,String topicOutTable, String topicDropIfExist
			,String docTopicTableDropIfExists,String docTopicOutSchema,String docTopicOutTable)
			throws SQLException {

			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDADB2.pldaTrain():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(modelOutSchema) + "','" + StringHandler.doubleQ(modelOutTable) + "')");
				st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" +StringHandler.doubleQ( modelOutSchema) +"','"+StringHandler.doubleQ(modelOutTable)+"')");
			}
			if (topicDropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDADB2.pldaTrain():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(topicOutSchema) + "','" + StringHandler.doubleQ(topicOutTable) + "')");
				st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(topicOutSchema) +"','"+StringHandler.doubleQ(topicOutTable)+"')");
			}
			if (topicDropIfExist.equalsIgnoreCase(Resources.YesOpt))
			{
				itsLogger.debug("PLDADB2.pldaTrain():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(docTopicOutSchema) + "','" + StringHandler.doubleQ(docTopicOutTable) + "')");
				st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(docTopicOutSchema) +"','"+StringHandler.doubleQ(docTopicOutTable)+"')");
			}
			
		
			StringBuffer sql = new StringBuffer();
			sql.append("call ")
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
			.append(",'").append(StringHandler.doubleQ(modelOutSchema)).append(".").append(StringHandler.doubleQ(PLDAPreString+timeStamp))
			.append("','").append(StringHandler.doubleQ(modelOutSchema)).append(".").append(StringHandler.doubleQ(PLDANoArrayPreString+timeStamp))
			.append("','").append(StringHandler.doubleQ(modelOutSchema)).append(".").append(StringHandler.doubleQ(modelOutTable))
			.append("','").append(StringHandler.doubleQ(docTopicOutSchema)).append(".").append(StringHandler.doubleQ(docTopicOutTable))
			.append("','").append(StringHandler.doubleQ(topicOutSchema)).append(".").append(StringHandler.doubleQ(topicOutTable))
			.append("')");
			itsLogger.debug("PLDADB2.pldaTrain():sql=" + sql.toString());

			st.execute(sql.toString());
			
			itsLogger.debug("PLDADB2.pldaTrain():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(modelOutSchema) + "','" + StringHandler.doubleQ(PLDAPreString + timeStamp) + "')");
			st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(modelOutSchema) +"','"+StringHandler.doubleQ(PLDAPreString+timeStamp)+"')");

			itsLogger.debug("PLDADB2.pldaTrain():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(modelOutSchema) + "','" + StringHandler.doubleQ(PLDANoArrayPreString + timeStamp) + "')");
			st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(modelOutSchema) +"','"+StringHandler.doubleQ(PLDANoArrayPreString+timeStamp)+"')");

	}




}
