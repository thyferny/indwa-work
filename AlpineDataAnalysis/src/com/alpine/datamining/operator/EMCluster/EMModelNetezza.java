 

package com.alpine.datamining.operator.EMCluster;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.resources.AlpineStoredProcedure;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

 public class EMModelNetezza extends EMModel {
     private static Logger itsLogger= Logger.getLogger(EMModelNetezza.class);

     private static final long serialVersionUID = -1955767291886970450L;
	private static final String AlpineMinerEMInfoTable = "alpine_miner_eminfo";

	public EMModelNetezza(DataSet trainingDataSet) {
		super(trainingDataSet);
	}

	@Override
	public void EMClusterPredict(String predictTable, String appendOnlyString,
			String endingString,Connection conncetion, String dropIfExist,
			String schemaName, String tableName) throws SQLException {
		Statement st = null;

		try {
			long timeStamp = System.currentTimeMillis();
			StringBuffer temptablename=new StringBuffer(AlpineMinerEMClusterTable+timeStamp);
			StringBuffer columntablename=new StringBuffer(AlpineMinerEMColumnTable+timeStamp);
			StringBuffer infotablename=new StringBuffer(AlpineMinerEMInfoTable+timeStamp);
			
			Iterator<Column> ColumnIterator = getTrainingHeader().getColumns()
					.iterator();
			int columnsize = getTrainingHeader().getColumns().size();
			st = conncetion.createStatement();
			int clusterNumber = this.getClusterNumber();
			String dropifexists = null;
			if (dropIfExist.equalsIgnoreCase(Resources.YesOpt)) {
				dropifexists = "call droptable_if_existsdoubleq('"+ StringHandler.doubleQ(tableName) + "')";
				itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + dropifexists);
				st.execute(dropifexists);
			}
			
			dropifexists = "call droptable_if_existsdoubleq('"
				+StringHandler.doubleQ(infotablename.toString())+"')";
			st.execute(dropifexists);
			dropifexists = "call droptable_if_existsdoubleq('"
				+StringHandler.doubleQ(columntablename.toString())+"')";
			st.execute(dropifexists);

			String createSQL = "create table "
					+ StringHandler.doubleQ(infotablename.toString())
					+ "	(  valueinfo double,id integer)";
			itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + createSQL);
			st.execute(createSQL);
			createSQL = "create table "
				+ StringHandler.doubleQ(columntablename.toString())
				+ "	(  valueinfo varchar(1024),id integer)";
			itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + createSQL);
			st.execute(createSQL);

			String[] ColumnArray = new String[columnsize];
			int i = 0;

			while (ColumnIterator.hasNext()) {
				
				Column tmp=ColumnIterator.next();
				String distinctValue = this.getAllTransformMap_valueKey().get(tmp.getName());
				String columnValues =  null;
				if(distinctValue!=null){
					String originColumnName=tmp.getName().substring(0,tmp.getName().length()-distinctValue.length()-1);
					columnValues="case when "+originColumnName+"=''''"+distinctValue+"'''' then 1 else 0 end";
				}
				else{
					columnValues =   tmp.getName();
					columnValues = StringHandler.doubleQ(columnValues);
				}
				
				ColumnArray[i] = tmp.getName();
				i++;
				String insertSQL = "insert into "
					+ StringHandler.doubleQ(columntablename.toString()) + " values ('"
					+   columnValues + "'," + i
					+ ")";
				itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + insertSQL);
				st.execute(insertSQL);
			}

			for (i = 0; i < clusterNumber; i++) {
				String insertSQL = "insert into "
						+ StringHandler.doubleQ(infotablename.toString()) + " values ("
						+ this.getClusteInfo().get(i+1).getAlpha() + "," + i
						+ ")";
				itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + insertSQL);
				st.execute(insertSQL);
				for (int j = 0; j < columnsize; j++) {
					int tempmuid = i * columnsize + j + clusterNumber;
					int tempsigmaid = i * columnsize + j + clusterNumber
							* columnsize + clusterNumber;
					insertSQL = "insert into "
							+ StringHandler.doubleQ(infotablename.toString())
							+ " values ("
							+ this.getClusteInfo().get(i+1).getMuValue().get(
									ColumnArray[j]) + "," + tempmuid + ")";
					itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + insertSQL);
					st.execute(insertSQL);
					insertSQL = "insert into "
							+ StringHandler.doubleQ(infotablename.toString())
							+ " values ("
							+ this.getClusteInfo().get(i+1).getSigmaValue().get(
									ColumnArray[j]) + "," + tempsigmaid + ")";
					itsLogger.debug("EMModelNetezza.EMClusterPredict():sql=" + insertSQL);
					st.execute(insertSQL);
				}
			}

			timeStamp = System.currentTimeMillis();

			StringBuffer sql = new StringBuffer();

			sql.append("select ").append(
					AlpineStoredProcedure.EMCLUSTER_PREDICT_STRING)
					.append("('").append(StringHandler.doubleQ(schemaName))
					.append(".").append(StringHandler.doubleQ(tableName))
					.append("','").append(predictTable).append("','")
					.append(StringHandler.doubleQ(columntablename.toString())).append("','")
					.append(StringHandler.doubleQ(infotablename.toString())).append("',")
					.append(clusterNumber).append(",'")
					.append(StringHandler.doubleQ(temptablename.toString())).append("')");
					itsLogger.debug("EMModelNetezza.EMClusterPredict():sql="
                            + sql.toString());

			st.executeQuery(sql.toString());

		} catch (SQLException e) {
			itsLogger.debug("EMModelNetezza.EMClusterPredict()" + e);
			throw new SQLException(e);

		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException e) {
				itsLogger.debug(e.toString());
				throw e;
			}

		}

	}
}
