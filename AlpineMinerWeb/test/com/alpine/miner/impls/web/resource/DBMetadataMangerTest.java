/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * PublishFlowTest.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jul 3, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.utility.db.DBMetaDataUtil;
import com.alpine.utility.db.DBMetadataManger;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author sam_zang
 * 
 */
public class DBMetadataMangerTest {
	private static final String SCHEMA_NAME_DEMO = "demo";

	DbConnection conn1 = new DbConnection(DataSourceInfoPostgres.dBType,
			 "114.132.246.180", 5432, "miner_demo", "miner_demo",   "miner_demo","false");

	DbConnection conn2 = new DbConnection(DataSourceInfoPostgres.dBType,
			 "114.132.246.180", 5432, "miner_demo", "miner_demo",   "miner_demo"	,"false");

	DBMetadataManger dbm = DBMetadataManger.INSTANCE;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeDBMetadataMangerTest() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterDBMetadataMangerTest() throws Exception {
	}

	@Test
	public void testGetSchemas() throws Exception {
		
		dbm.refreshDBConnection(conn1,false,true);
		List<String> sl1 = dbm.getSchemaList(conn1);
		List<String> sl2 = dbm.getSchemaList(conn2); 
		List<String> sl3 = dbm.getSchemaList(conn1);
		Assert.assertEquals(sl1, sl2) ;
		Assert.assertEquals(sl1, sl3) ;
		Assert.assertEquals(sl3, sl2) ;
		 
	}
	
	@Test
	public void testGetTables() throws Exception {
		
		dbm.refreshDBConnection(conn1,false,true);
		List<String> sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
		List<String> sl3 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
		List<String> sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
		Assert.assertEquals(sl1, sl2) ;
		Assert.assertEquals(sl1, sl3) ;
		Assert.assertEquals(sl3, sl2) ;
		
		
		 
	}
 
	
	@Test
	public void testRefreshConn() throws Exception {
		String schemName="zy_testschema" ;
		dbm.refreshDBConnection(conn1,false,true);
		List<String> sl1 = dbm.getSchemaList(conn1);
		List<String> sl2 = dbm.getSchemaList(conn2); 
		Assert.assertFalse(sl1.contains(schemName)) ;
		DBMetaDataUtil dmd = new DBMetaDataUtil(conn1);
		try{
			Connection conn = dmd.getConnection();
			Statement st = conn.createStatement();
			
			st.execute("CREATE SCHEMA "+schemName+" AUTHORIZATION miner_demo");
			sl2 = dbm.getSchemaList(conn2); 
			Assert.assertFalse(sl2.contains(schemName)) ;
			 
			dbm.refreshDBConnection(conn1, false,true) ;
			
			sl2 = dbm.getSchemaList(conn2); 
			Assert.assertTrue(sl2.contains(schemName)) ;
 
	 		st.execute(" DROP SCHEMA " +schemName);
	 		sl2 = dbm.getSchemaList(conn2); 
			Assert.assertTrue(sl2.contains(schemName)) ;

			dbm.refreshDBConnection(conn1, false,true) ;
			sl2 = dbm.getSchemaList(conn2); 
			Assert.assertFalse(sl2.contains(schemName)) ;
			
		}catch(Exception e){
			throw e;
		}finally{
			dmd.disconnect();
		}
		
	}
	@Test
	public void testRefreshSchema() throws Exception {
		
		dbm.refreshDBConnection(conn1,false,true);
		List<String> sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
		List<String> sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
		Assert.assertEquals(sl1, sl2) ;
		String newTableName="table_"+System.currentTimeMillis();
		Assert.assertEquals(sl1.contains(newTableName),false);
		DBMetaDataUtil dmd = new DBMetaDataUtil(conn1);
		StringBuffer createSql = new StringBuffer();
		createSql.append("create table ").append("demo."+newTableName).append("(column1 float");
		createSql.append(",column2  float)");
		try{
			Connection conn = dmd.getConnection();
			Statement st = conn.createStatement();
			
			st.execute(createSql.toString());
			sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
			sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
			Assert.assertEquals(sl1.contains(newTableName),false);
			Assert.assertEquals(sl2.contains(newTableName),false);
			
			sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
			sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
			dbm.refreshSchema(conn1, SCHEMA_NAME_DEMO,true);
			sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
			sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
			Assert.assertEquals(sl1.contains(newTableName),true);
			Assert.assertEquals(sl2.contains(newTableName),true);
			
			StringBuffer dropSql = new StringBuffer();
			dropSql.append("drop table ");
	 		dropSql.append("demo."+newTableName);
	 		st.execute(dropSql.toString());
			
	 		sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
			sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
			Assert.assertEquals(sl1.contains(newTableName),true);
			Assert.assertEquals(sl2.contains(newTableName),true);
			
			dbm.refreshSchema(conn1, SCHEMA_NAME_DEMO,true);
			
			sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
			sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
			Assert.assertEquals(sl1.contains(newTableName),false);
			Assert.assertEquals(sl2.contains(newTableName),false);
			
		}catch(Exception e){
			throw e;
		}finally{
			dmd.disconnect();
		}
		 
	}
	
	@Test
	public void testRefreshTable() throws Exception {
		
		dbm.refreshDBConnection(conn1,false,true);
		String columnName_test = "column3" ;
 
		String newTableName="table_"+System.currentTimeMillis();
	 
		DBMetaDataUtil dmd = new DBMetaDataUtil(conn1);
		StringBuffer createSql = new StringBuffer();
		createSql.append("create table ").append("demo."+newTableName).append("(column1 float");
		createSql.append(",column2  float)");
		try{
			Connection conn = dmd.getConnection();
			Statement st = conn.createStatement();
			
			st.execute(createSql.toString());
			 
			
			dbm.refreshSchema(conn1, SCHEMA_NAME_DEMO,true);
			dbm.refreshTable(conn1, SCHEMA_NAME_DEMO, newTableName,true) ;
			List<TableColumnMetaInfo> tableColumn = dbm.getTableColumnInfoList(conn1, SCHEMA_NAME_DEMO, newTableName);
			Assert.assertTrue(tableColumn.size()==2) ;
			StringBuffer alterSql = new StringBuffer();
			alterSql.append("alter table ").append("demo."+newTableName).append(" ADD COLUMN column3 varchar(30)");
		
			st.execute(alterSql.toString());
			st.close();
			conn.commit();
			tableColumn = dbm.getTableColumnInfoList(conn1, SCHEMA_NAME_DEMO, newTableName);
			Assert.assertTrue(tableColumn.size()==2) ;
			
			dbm.refreshTable(conn1, SCHEMA_NAME_DEMO, newTableName,true) ;
			tableColumn = dbm.getTableColumnInfoList(conn1, SCHEMA_NAME_DEMO, newTableName);
			Assert.assertTrue(tableColumn.size()==3) ;
			
		}catch(Exception e){
			throw e;
		}finally{
			dmd.disconnect();
		}
		 
	}
	
	@Test
	public void testRemoveTableFromCache() throws Exception {
		
		dbm.refreshDBConnection(conn1,false,true);
		List<String> sl1 = dbm.getTableAndViewNameList(conn1, SCHEMA_NAME_DEMO);
	
	 
		String sampleTablename=sl1.get(0) ;
		List<String> sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
		Assert.assertTrue(sl2.contains(sampleTablename)) ;
		
		dbm.removeTableFromCache(conn1, SCHEMA_NAME_DEMO, sampleTablename);
		
		sl2 = dbm.getTableAndViewNameList(conn2, SCHEMA_NAME_DEMO);
		Assert.assertFalse(sl2.contains(sampleTablename)) ;
		 
	}
	
	 
}
