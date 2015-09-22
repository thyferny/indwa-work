/**
 * ClassName DataOperationAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 2, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
/**
 * @author Richie Lo
 *
 */
public abstract class DataOperationAnalyzer extends AbstractDBAttributeAnalyzer {
    private static final Logger itsLogger=Logger.getLogger(DataOperationAnalyzer.class);

    private String outputType;

	private String dropIfExist;
	private String inputSchema;
	private String inputTable;

	private ISqlGeneratorMultiDB sqlGenerator;
	@Override
	public abstract AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException;

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return outputType;
	}	

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}

	public String getDropIfExist() {
		return dropIfExist;
	}

	public void setInputSchema(String inputSchema) {
		this.inputSchema = inputSchema;
	}

	public String getInputSchema() {
		return inputSchema;
	}

	public void setInputTable(String inputTable) {
		this.inputTable = inputTable;
	}

	public String getInputTable() {
		return inputTable;
	}
 
	protected void dropIfExist(DataSet dataSet) throws OperatorException, AnalysisError {

		sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		
		String tableType;
		StringBuilder sql = new StringBuilder();
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		try {
			String[] tableTypes = { "TABLE", "VIEW" };
			DatabaseMetaData md = databaseConnection.getConnection().getMetaData();
			ResultSet rsTable = md.getTables(null, getOutputSchema(), "%",
					tableTypes);
			while (rsTable.next()) {
				if (getOutputTable().equals(rsTable.getString("TABLE_NAME"))) {
					tableType = rsTable.getString("TABLE_TYPE");
					if (!dropIfExist.equalsIgnoreCase("yes")) {
						rsTable.close();
						throw new AnalysisError(this,AnalysisErrorName.Drop_if_Exist,AlpineThreadLocal.getLocale(),tableType,getOutputSchema() + "." + getOutputTable());
					} else {
						if (tableType.equalsIgnoreCase("table")) {
							sql.append("drop table ");
						} else {
							sql.append("drop view ");
						}
						sql.append(getQuotaedTableName(getOutputSchema(), getOutputTable())).append(" ").append(sqlGenerator.cascade());
						Statement st = databaseConnection.createStatement(false);
						itsLogger.debug(
								"DataOperationAnalyzer.dropIfExist():sql="
										+ sql);
						st.execute(sql.toString());
					}
				}
			}
			rsTable.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}

