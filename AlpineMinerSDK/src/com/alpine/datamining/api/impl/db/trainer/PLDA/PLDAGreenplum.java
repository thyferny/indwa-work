/**
 * ClassName PLDAGreenplum.java
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

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 * 
 */
public class PLDAGreenplum extends PLDAImpl {
    private static final Logger itsLogger = Logger.getLogger(PLDAGreenplum.class);

    @Override
	public void pldaTrain(Connection conncetion, Statement st,
			String dicSchema, String dicTable, String dicIndexColumn,
			String dicContentColumn, String contentSchema, String contentTable,
			String contentColumn, String contentIDColumn, long timeStamp,
			String dropIfExist, double alpha, double beta,
			String modelOutSchema, String modelOutTable, long topicnumber,
			long iterationNumber, String topicOutSchema, String topicOutTable,
			String topicDropIfExist, String docTopicTableDropIfExists,
			String docTopicOutSchema, String docTopicOutTable)
			throws SQLException {
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory
				.createConnectionInfo(DataSourceInfoGreenplum.dBType);

		AnalysisStorageParameterModel modelOutputTableStorageParameters = null;
		String modelAppendOnlyString = "";
		String modelEndingString = " ";
		modelOutputTableStorageParameters = getPLDAModelOutputTableStorageParameters();
		if (modelOutputTableStorageParameters == null
				|| !modelOutputTableStorageParameters.isAppendOnly()) {
			modelAppendOnlyString = " ";
		} else {
			modelAppendOnlyString = sqlGenerator.getStorageString(
					modelOutputTableStorageParameters.isAppendOnly(),
					modelOutputTableStorageParameters.isColumnarStorage(),
					modelOutputTableStorageParameters.isCompression(),
					modelOutputTableStorageParameters.getCompressionLevel());
		}

		modelEndingString = sqlGenerator
				.setCreateTableEndingSql(modelOutputTableStorageParameters == null ? null
						: modelOutputTableStorageParameters
								.getSqlDistributeString());

		AnalysisStorageParameterModel topicOutputTableStorageParameters = null;
		String topicAppendOnlyString = "";
		String topicEndingString = "";
		topicOutputTableStorageParameters = getTopicOutTableStorageParameters();
		if (topicOutputTableStorageParameters == null
				|| !topicOutputTableStorageParameters.isAppendOnly()) {
			topicAppendOnlyString = " ";
		} else {
			topicAppendOnlyString = sqlGenerator.getStorageString(
					topicOutputTableStorageParameters.isAppendOnly(),
					topicOutputTableStorageParameters.isColumnarStorage(),
					topicOutputTableStorageParameters.isCompression(),
					topicOutputTableStorageParameters.getCompressionLevel());
		}

		topicEndingString = sqlGenerator
				.setCreateTableEndingSql(topicOutputTableStorageParameters == null ? null
						: topicOutputTableStorageParameters
								.getSqlDistributeString());

		AnalysisStorageParameterModel docTopicOutputTableStorageParameters = null;
		String docTopicAppendOnlyString = "";
		String docTopicEndingString = "";
		docTopicOutputTableStorageParameters = getDocTopicOutTableStorageParameters();
		if (docTopicOutputTableStorageParameters == null
				|| !docTopicOutputTableStorageParameters.isAppendOnly()) {
			docTopicAppendOnlyString = " ";
		} else {
			docTopicAppendOnlyString = sqlGenerator.getStorageString(
					docTopicOutputTableStorageParameters.isAppendOnly(),
					docTopicOutputTableStorageParameters.isColumnarStorage(),
					docTopicOutputTableStorageParameters.isCompression(),
					docTopicOutputTableStorageParameters.getCompressionLevel());
		}

		docTopicEndingString = sqlGenerator
				.setCreateTableEndingSql(docTopicOutputTableStorageParameters == null ? null
						: docTopicOutputTableStorageParameters
								.getSqlDistributeString());

		if (dropIfExist.equalsIgnoreCase(Resources.YesOpt)) {
			itsLogger.debug("PLDAGreenplum.pldaTrain():sql=Drop table IF EXISTS "
							+ StringHandler.doubleQ(modelOutSchema)
							+ "."
							+ StringHandler.doubleQ(modelOutTable));
			st.execute("Drop table IF EXISTS "
					+ StringHandler.doubleQ(modelOutSchema) + "."
					+ StringHandler.doubleQ(modelOutTable));
		}
		if (topicDropIfExist.equalsIgnoreCase(Resources.YesOpt)) {
			itsLogger.debug("PLDAGreenplum.pldaTrain():sql=Drop table IF EXISTS "
							+ StringHandler.doubleQ(topicOutSchema)
							+ "."
							+ StringHandler.doubleQ(topicOutTable));
			st.execute("Drop table IF EXISTS "
					+ StringHandler.doubleQ(topicOutSchema) + "."
					+ StringHandler.doubleQ(topicOutTable));
		}
		if (docTopicTableDropIfExists.equalsIgnoreCase(Resources.YesOpt)) {
			itsLogger.debug("PLDAGreenplum.pldaTrain():sql=Drop table IF EXISTS "
							+ StringHandler.doubleQ(docTopicOutSchema)
							+ "."
							+ StringHandler.doubleQ(docTopicOutTable));
			st.execute("Drop table IF EXISTS "
					+ StringHandler.doubleQ(docTopicOutSchema) + "."
					+ StringHandler.doubleQ(docTopicOutTable));
		}

		StringBuffer sql = new StringBuffer();
		sql.append("select ").append(AlpineStoredProcedure.PLDA_TRAIN_STRING)
				.append("('").append(StringHandler.doubleQ(contentSchema))
				.append(".").append(StringHandler.doubleQ(contentTable))
				.append("','").append(StringHandler.doubleQ(contentIDColumn))
				.append("','").append(StringHandler.doubleQ(contentColumn))
				.append("',").append(alpha).append(",").append(beta)
				.append(",").append(topicnumber).append(",'").append(
						StringHandler.doubleQ(dicSchema)).append(".").append(
						StringHandler.doubleQ(dicTable)).append("','").append(
						StringHandler.doubleQ(dicContentColumn)).append("',")
				.append(iterationNumber).append(",'").append(
						StringHandler.doubleQ(PLDAPreString + timeStamp))
				.append("','").append(
						StringHandler.doubleQ(PLDAPreString + timeStamp
								+ PLDANoArrayPreString)).append("','").append(
						StringHandler.doubleQ(modelOutSchema)).append(".")
				.append(StringHandler.doubleQ(modelOutTable)).append("','")
				.append(modelAppendOnlyString).append("','")
				.append(modelEndingString).append("','")
				.append(StringHandler.doubleQ(topicOutSchema)).append(".")
				.append(StringHandler.doubleQ(topicOutTable)).append("','")
				.append(topicAppendOnlyString).append("','")
				.append(topicEndingString).append("','")
				.append(StringHandler.doubleQ(docTopicOutSchema)).append(".")
				.append(StringHandler.doubleQ(docTopicOutTable)).append("','")
				.append(docTopicAppendOnlyString).append("','")
				.append(docTopicEndingString)
				.append("')");
		itsLogger.debug("PLDAGreenplum.pldaTrain():sql=" + sql.toString());

		st.executeQuery(sql.toString());

	}

}
