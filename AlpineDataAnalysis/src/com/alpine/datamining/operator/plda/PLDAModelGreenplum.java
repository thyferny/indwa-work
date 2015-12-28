


package com.alpine.datamining.operator.plda;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


public class PLDAModelGreenplum extends PLDAModel{
    private static final Logger itsLogger = Logger.getLogger(PLDAModelGreenplum.class);

    
	private static final long serialVersionUID = -9113653305848033520L;

	
	public PLDAModelGreenplum(DataSet trainingDataSet) {
		super(trainingDataSet);
		}




	@Override
	public void PLDAPredict(String predictTable,long iterationNumber,  
			String docOutSchema, String docOutTable, String appendOnlyString, String endingString, String docTopicOutSchema,
			String docTopicOutTable, String docTopicAppendOnlyString, String docTopicEndingString,  Connection conncetion, String dropIfExist,
			String dropIfExistRTable) throws SQLException {
			Statement st=null;
			
			try {
				
				st = conncetion.createStatement();
				if (dropIfExist.equalsIgnoreCase(Resources.YesOpt))
				{
					itsLogger.debug("PLDAModelDB2.pldaPredict():sql=Drop table IF EXISTS "+StringHandler.doubleQ(docOutSchema)+"."+StringHandler.doubleQ(docOutTable));
					
					st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(docOutSchema)+"."+StringHandler.doubleQ(docOutTable));
				}
				if (dropIfExistRTable.equalsIgnoreCase(Resources.YesOpt))
				{
					itsLogger.debug("PLDAModelDB2.pldaPredict():sql=Drop table IF EXISTS "+StringHandler.doubleQ(docTopicOutSchema)+"."+StringHandler.doubleQ(docTopicOutTable));
					
					st.execute("Drop table IF EXISTS "+StringHandler.doubleQ(docTopicOutSchema)+"."+StringHandler.doubleQ(docTopicOutTable));
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
				.append("','").append(appendOnlyString)
				.append("','").append(endingString)
				.append("','").append(StringHandler.doubleQ(docTopicOutSchema)).append(".").append(StringHandler.doubleQ(docTopicOutTable))
				.append("','").append(docTopicAppendOnlyString)
				.append("','").append(docTopicEndingString)
				.append("')");
				itsLogger.debug("PLDAModelGreenplum.PLDAPredict():sql="+sql.toString());

				st.executeQuery(sql.toString());

				
			} catch (SQLException e) {
				itsLogger.debug("PLDAModelGreenPlumn.PLDAPredict()"+e);
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
