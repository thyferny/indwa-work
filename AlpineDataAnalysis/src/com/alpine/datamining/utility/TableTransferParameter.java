/**
 * ClassName TableTransferParameter.java
 *
 * Version information: 1.00
 *
 * Data: 2011-12-20
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;
/**
 * This tranfer parameter through table.
 * @author Eason Yu
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.exception.OperatorException;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class TableTransferParameter {
    private static Logger itsLogger= Logger.getLogger(TableTransferParameter.class);
    public static void createDoubleTable(String tableName, Statement st) throws OperatorException{
		String sql = "create table "+tableName+" ( id int, value double )";
		try {
			itsLogger.debug("TableTransferParameter.createDoubleTable():sql="
					+ sql);
			st.execute(sql);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	public static  void createStringTable(String tableName, Statement st) throws OperatorException{
		String sql = "create table "+tableName+" ( id int, value varchar(64000) )";
		try {
			itsLogger.debug("TableTransferParameter.createStringTable():sql="
					+ sql);
			st.execute(sql);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	public static  void insertTable(String tableName, Statement st, double[] value) throws OperatorException{
		if(value == null){
			return;
		}
		for(int i = 0; i < value.length; i++){
			String sql = "insert into "+tableName+" values( "+i+","+value[i]+"::double)";
			itsLogger.debug("TableTransferParameter.insertTable():sql="
					+ sql);
			try {
				st.execute(sql);
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}
	public static  void insertTable(String tableName, Statement st, Double[] value) throws OperatorException{
		if(value == null){
			return;
		}
		for(int i = 0; i < value.length; i++){
			String sql = "insert into "+tableName+" values( "+i+","+value[i].doubleValue()+"::double)";
			itsLogger.debug("TableTransferParameter.insertTable():sql="
					+ sql);
			try {
				st.execute(sql);
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}
	public static  void insertTable(String tableName, Statement st, Integer[] value) throws OperatorException{
		if(value == null){
			return;
		}
		for(int i = 0; i < value.length; i++){
			String sql = "insert into "+tableName+" values( "+i+","+value[i]+")";
			try {
				itsLogger.debug("TableTransferParameter.insertTable():sql="
						+ sql);
				st.execute(sql);
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}

	public static  double[] getResult(String tableName, Statement st) throws SQLException{
		String sql = "select value from "+tableName+" order by id ";
		double[] result = null;
		ArrayList<Double> doubleArray = new ArrayList<Double>();
		itsLogger.debug("TableTransferParameter.getResult():sql="
				+ sql);
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()){
			double doubleValue = rs.getDouble(1);
			doubleArray.add(doubleValue);
		}
		result = new double[doubleArray.size()];
		for(int i = 0; i < doubleArray.size(); i++){
			result[i] = doubleArray.get(i);
		}
		return result;
	}
	public static  Double[] getDoubleResult(String tableName, Statement st) throws SQLException{
		String sql = "select value from "+tableName+" order by id ";
		Double[] result = new Double[0];
		ArrayList<Double> doubleArray = new ArrayList<Double>();
		itsLogger.debug("TableTransferParameter.getDoubleResult():sql="
				+ sql);
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()){
			double doubleValue = rs.getDouble(1);
			doubleArray.add(doubleValue);
		}
		return doubleArray.toArray(result);
	}
	public static  void insertTable(String tableName, Statement st, String[] value) throws OperatorException{
		if(value == null){
			return;
		}
		for(int i = 0; i < value.length; i++){
			String sql = "insert into "+tableName+" values( "+i+",'"+StringHandler.escQ(value[i])+"')";
			try {
				itsLogger.debug("TableTransferParameter.insertTable():sql="
						+ sql);
				st.execute(sql);
			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}
	public static void truncateTable(String tableName, Statement st)throws OperatorException{
		String sql = "truncate table "+tableName;
		try {
			itsLogger.debug("TableTransferParameter.truncateTable():sql="
					+ sql);
			st.execute(sql);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	
	public static  void dropResultTable(String tableName, Statement st) throws OperatorException{
		String sql = "drop table "+tableName;
		try {
			itsLogger.debug("TableTransferParameter.dropResultTable():sql="
					+ sql);
			st.execute(sql);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

}
