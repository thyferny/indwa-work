


package com.alpine.datamining.operator.plda;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


public class PLDAModelDB2 extends PLDAModel{
    private static Logger itsLogger= Logger.getLogger(PLDAModelDB2.class);

    
	private static final long serialVersionUID = 8639840140354507851L;

	
	public PLDAModelDB2(DataSet trainingDataSet) {
		super(trainingDataSet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void PLDAPredict(String predictTable, long iterationNumber,
			String docOutSchema, String docOutTable, String appendOnlyString, String endingString, String topicOutSchema,
			String topicOutTable,String docTopicAppendOnlyString, String docTopicEndingString,  Connection conncetion, String dropIfExist,
			String dropIfExistRTable) throws SQLException {
			Statement st=null;
			try {
				st = conncetion.createStatement();
				if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
				{
					itsLogger.debug("PLDAModelDB2.pldaPredict():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(docOutSchema) + "','" + StringHandler.doubleQ(docOutTable) + "')");
					st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(docOutSchema) +"','"+StringHandler.doubleQ(docOutTable)+"')");
				}
				if (dropIfExistRTable.equalsIgnoreCase(Resources.YesOpt))
				{
					itsLogger.debug("PLDAModelDB2.pldaPredict():sql=call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(topicOutSchema) + "','" + StringHandler.doubleQ(topicOutTable) + "')");
					st.execute(" call PROC_DROPSCHTABLEIFEXISTS('" + StringHandler.doubleQ(topicOutSchema) +"','"+StringHandler.doubleQ(topicOutTable)+"')");
				}
				StringBuffer sql=new StringBuffer();
				sql.append("call ")
				.append(AlpineStoredProcedure.PLDA_PREDICT_STRING)
				.append("('")
				.append(StringHandler.doubleQ(this.getModelSchema()))
				.append(".")
				.append(StringHandler.doubleQ(this.getModelTable()))
				.append("','").append(StringHandler.doubleQ(predictTable))
				.append("','").append(StringHandler.doubleQ(this.getDocIdColumn()))
				.append("','").append(StringHandler.doubleQ(this.getDocContentColumn()))
				.append("',").append(this.getAlpha())
				.append(",").append(this.getBeta())
				.append(",").append(this.getTopicNumber())
				.append(",").append(iterationNumber)
				.append(",'")
				.append(StringHandler.doubleQ(this.getDictSchema()))
				.append(".").append(StringHandler.doubleQ(this.getDictTable()))
				.append("','").append(StringHandler.doubleQ(this.getDicContentColumn()))
				.append("','").append(StringHandler.doubleQ(this.getDicIdColumn()))
				.append("','").append(StringHandler.doubleQ(docOutSchema)).append(".").append(StringHandler.doubleQ(docOutTable))
				.append("','").append(StringHandler.doubleQ(topicOutSchema)).append(".").append(StringHandler.doubleQ(topicOutTable))
				.append("')   ");
				itsLogger.debug("PLDAModelDB2.PLDAPredict():sql=" + sql.toString());

				st.execute(sql.toString());
							
			} catch (SQLException e) {
				itsLogger.debug("PLDAModelDB2.PLDAPredict()" + e);
				throw new SQLException(e);
				
			}finally
			{
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
