/**
 * ClassName AlpineUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPasswordField;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.alpine.resources.CommonLanguagePack;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;


public class AlpineUtil {

 private static final String PASSWORD = "password";
 private static final String USER = "user";
 private static final String alpine_cluster = "alpine_cluster";
 
  public static String jarFileDir = AlpineUtil.getCurrentDirectory()+File.separator+"plugins"+File.separator +"AlpineMinerUI_1.0.0"+File.separator+"lib";
// public static String jdbcFilePath = "file:///Users/john/adl_eclipse_new/AlpineCommonPlugin/lib";
private static final Logger itsLogger = Logger.getLogger(AlpineUtil.class);

    public static String getJarFileDir() {
	 String jdbcFolder = System.getenv("Alpine_jdbcFilePath");
	 if(StringUtil.isEmpty(jdbcFolder)==false){
		 if(jdbcFolder.endsWith(File.separator)==false){
			 return jdbcFolder +File.separator;
		 }
		 else{
			 return jdbcFolder;
		 }
	 }else{
		 if(jarFileDir.endsWith(File.separator)==false){
			 jarFileDir= jarFileDir +File.separator;
		 }
		 
		 jarFileDir= new File(jarFileDir).getAbsolutePath() +File.separator;
		 return AlpineUtil.jarFileDir;
	 }

 }
 public static void setJarFileDir(String jdbcFilePath) {
  AlpineUtil.jarFileDir=jdbcFilePath;
 }

 
 public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');
 
        if (f.isDirectory())
         ext = null;
        else if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

 public static boolean isInteger(String value) {
  try {
   Integer.parseInt(value);
   return true;
  } catch (NumberFormatException e) {
   return false;
  }
 }

 /**
  * assemble the password from the JPasswordField
  * @param p: password in JPasswordField
  * @return password in String
  */
 static public String getPasswordField(JPasswordField p) {
  String password="";
  for (char charItem : p.getPassword()) {
   password += charItem;
  }
  return password;
 }

 public static boolean isPositiveInteger(String theString) {
  try {
   int num =Integer.parseInt(theString);
   return (num >=0);
  } catch (NumberFormatException e) {
   return false;
  }
 }

 public static boolean isVariableName(String theString) {
  return matchRegex(theString,"[a-zA-Z_][\\w\\s]*");
 }

 public static boolean matchRegex(String theString, String regex) {
  Pattern p = Pattern.compile(regex);
  Matcher m = p.matcher(theString);
  return m.matches();
 }

 public static String objectToString(Object obj) {
  BASE64Encoder encode = new BASE64Encoder();
  String out = null;
  if (obj != null) {
   try {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    out = encode.encode(baos.toByteArray());
   } catch (IOException e) {
   e.printStackTrace();
   return null;
   }
  }
  return out;
 }
 
 public static Object stringToObject(String str) {
  BASE64Decoder decode = new BASE64Decoder();

  Object out = null;
  if (str != null) {
   try {  
    ByteArrayInputStream bios = new ByteArrayInputStream(decode.decodeBuffer(str));
    ObjectInputStream ois = new ObjectInputStream(bios);
    out = ois.readObject();
   } catch (Exception e) {
    e.printStackTrace();
    return null;
   }
  }
  return out;
 }

 public static boolean isNumber(String value) {
  try {
   double val = Double.parseDouble(value);
   return true;
  } catch (NumberFormatException e) {
   return false;
  }
 }
 public static boolean isFNumber(String value){
  try {
   double val = Double.parseDouble(value);
   if(val<0){
    return true;
   }else{
    return false;
   }
  } catch (NumberFormatException e) {
   return false;
  }
 }
 public static boolean isFloat(String value){
  try{
   float f = Float.parseFloat(value);
   return true;
  }catch(Exception e){
   return false;
  }
 }
 
// public static Connection createConnection(String analyzerName,String userName, String password,
//   String url, String system) throws  Exception {
//  return createConnection(analyzerName,userName,password,url,system,Locale.getDefault());
// }
 
 public static Connection createConnection( DbConnection dbConn ) throws  Exception {
  return createConnection(dbConn,Locale.getDefault());
 }
 
 
 public static Connection createConnection(  DbConnection dbConn, Locale locale) throws  Exception {
  String userName = dbConn.getDbuser();
  String password = dbConn.getPassword();
  String url = dbConn.getUrl();
  String system =dbConn.getDbType();
     String useSSL = dbConn.getUseSSL();
  return createConnection( userName, password, url, system,locale ,useSSL);
 }
 public static Connection createConnection(  String userName,
   String password, String url, String system, Locale locale ,String useSSL) throws SQLException {
  Connection connection=null;
  String driveClass=null;
  if (system.equals( DataSourceInfoPostgres.dBType)
  ||system.equals( DataSourceInfoGreenplum.dBType)) {
   driveClass=DataSourceInfoPostgres.dBDriver; 
   try {
    Class.forName(driveClass);
   } catch (ClassNotFoundException e) {
    itsLogger.error(e.getMessage(),e);
    throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_Driver_Not_Found, locale));
   }
   
   try {
    DriverManager.setLoginTimeout(
               Integer.parseInt(
                 ProfileReader.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
    
       url = url+"?user="+userName+"&password="+password;
              if("true".equals(useSSL)){
               url = url + "&ssl=true";
              }
              connection  = DriverManager.getConnection(url);
    
   } catch (SQLException e) {
    itsLogger.error(e.getMessage(),e);
    throw e;
   }
  } else if (system.equals(DataSourceInfoOracle.dBType)) {
   if(OracleJdbcDriver.getInstance()==null){
    throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_Driver_Not_Found, locale));
   }
          Driver driverd  = OracleJdbcDriver.getInstance().getDriver();
             Properties props = new Properties();
              props.setProperty(USER,  userName);
              props.setProperty(PASSWORD, password);
              try {
               DriverManager.setLoginTimeout(
                 Integer.parseInt(
                   ProfileReader.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
               connection=driverd.connect(url, props);
     } catch (SQLException e) {
      itsLogger.error(e.getMessage(),e);
     }
  } else if (system.equals(DataSourceInfoDB2.dBType)) {
   if(Db2JdbcDriver.getInstance()==null){
    throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_Driver_Not_Found, locale));
   }
   
         Driver driverd  = Db2JdbcDriver.getInstance().getDriver();
            Properties props = new Properties();
             props.setProperty(USER,  userName);
             props.setProperty(PASSWORD, password);
             try {
              DriverManager.setLoginTimeout(
                Integer.parseInt(
                  ProfileReader.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
              connection=driverd.connect(url, props);
    } catch (SQLException e) {
     itsLogger.error(e.getMessage(),e);
    }
  } else if (system.equals(DataSourceInfoNZ.dBType)) {
   if(NZJdbcDriver.getInstance()==null){
    throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.JDBC_Driver_Not_Found, locale));
   }
   
         Driver driverd  = NZJdbcDriver.getInstance().getDriver();
            Properties props = new Properties();
             props.setProperty(USER,  userName);
             props.setProperty(PASSWORD, password);
             try {
              DriverManager.setLoginTimeout(
                Integer.parseInt(
                  ProfileReader.getInstance().getParameter(ProfileUtility.DB_CONN_TIMEOUT)));
              connection=driverd.connect(url, props);
    } catch (SQLException e) {
     itsLogger.error(e.getMessage(),e);
    }
  } else {
    throw new RuntimeException(CommonLanguagePack.getMessage(CommonLanguagePack.Unsupported_Database_type, locale));   
  }
  

  return connection;
 }
  
// public static Connection createConnection( String userName, String password,
//   String url, String system) throws  Exception {
//  return createConnection(userName,password,url,system,Locale.getDefault());
// }
// public static Connection createConnection( String userName, String password,
//   String url, String system, Locale locale) throws  Exception {
//  
//  return createConnection(null,  userName,   password,
//      url,   system,locale);
//     
// }
 
 public static String getHostIP(String name)  {
  String address = null;
  try {
     address =  InetAddress.getByName(name).getHostAddress();
  } catch (UnknownHostException e) {
     address = name;
  }
  return address;
 }
 
 public static String getCurrentDirectory(){

  String operatSystem=System.getProperty("os.name");
  if(operatSystem.startsWith("Windows")){
   return "."+File.separator;
  }else if(operatSystem.startsWith("Mac OS")){
   return ".."+File.separator+".."+File.separator+".."+File.separator;
  }else if(operatSystem.startsWith("Linux")){
   return "."+File.separator;
  }else {
   return null;
  }
 }
 public static String dealNullValue(ResultSet rs,int i) throws SQLException{
  double dou=rs.getDouble(i);
  if(!rs.wasNull()){
   return AlpineMath.doubleExpression(dou);
  }else{
   return "";
  }
 }
 
 
 public static String converterDateType(String type,String dataSource){
  if(dataSource.equals(DataSourceInfoPostgres.dBType)
  || dataSource.equals(DataSourceInfoGreenplum.dBType)){
   if(type.equalsIgnoreCase("number")){
    return GPSqlType.NUMERIC;
   }else if(type.equalsIgnoreCase("integer")){
    return GPSqlType.BIGINT;
   }else if(type.equalsIgnoreCase("date")){
    return GPSqlType.DATE;
   }else if(type.equalsIgnoreCase("text")){
    return GPSqlType.TEXT;
   }else if(type.equalsIgnoreCase("array")){
    return GPSqlType.ARRAY;
   }else{
    return null;
   }  
  }else if(dataSource.equals(DataSourceInfoOracle.dBType)){
   if(type.equalsIgnoreCase("number")){
    return OraSqlType.NUMBER;
   }else if(type.equalsIgnoreCase("integer")){
    return OraSqlType.INTEGER;
   }else if(type.equalsIgnoreCase("date")){
    return OraSqlType.DATE;
   }else if(type.equalsIgnoreCase("text")){
    return OraSqlType.TEXT;
   }if(type.equalsIgnoreCase("array")){
    return OraSqlType.ARRAY;
   }else {
    return null;
   } 
  }
  return null;
 }
 public static String getArrayName(String type,String dataSource,String columName){
  if(dataSource.equals(DataSourceInfoPostgres.dBType)
  ||dataSource.equals(DataSourceInfoGreenplum.dBType)){
    return "array["+columName+"]"; 
  }else if(dataSource.equals(DataSourceInfoOracle.dBType)){
   if(type.equalsIgnoreCase("number")){
    return "Floatarray("+columName+")";
   }else if(type.equalsIgnoreCase("integer")){
    return "IntegerArray("+columName+")";
   }else if(type.equalsIgnoreCase("date")){
    return null;
   }else if(type.equalsIgnoreCase("text")){
    return "varchar2array("+columName+")";
   }else {
    return null;
   } 
  }
  return null;
 }
 
 public static String dealArrayArray(ResultSet rs, int i)
  throws SQLException {
   Object[] arrayarray = (Object[])rs.getArray(i).getArray();
   StringBuilder item = new StringBuilder("{");
   boolean first = true;
   if (arrayarray != null){
    for(int j = 0; j < arrayarray.length; j++){
     ResultSet array = ((Array)arrayarray[j]).getResultSet();
     ArrayList<Double> arrayDouble = new ArrayList<Double>();
     while(array.next()){
      arrayDouble.add(array.getInt(1) - 1, array.getDouble(2));
     }
     for(int k = 0; k < arrayDouble.size(); k++){
      if(first){
       first = false;
      }else{
       item.append(",");
      }
      if (arrayDouble.get(k) != null && !Double.isNaN(arrayDouble.get(k).doubleValue())){
       item.append(arrayDouble.get(k).doubleValue());
      }else{
       item.append(0);
      }
     }
    }
   }
   item.append("}");
   return item.toString();
 }
 
 public static String dealArray(ResultSet rs, int i)
 throws SQLException {
  StringBuilder item = new StringBuilder("{");
  String columnTypeName=rs.getMetaData().getColumnTypeName(i);
  if(columnTypeName.endsWith(OraSqlType.array_Types[0])){//float
   ResultSet array = ((Array)rs.getArray(i)).getResultSet();
   ArrayList<Double> arrayDouble = new ArrayList<Double>();
   while(array.next()){
    arrayDouble.add(array.getInt(1) - 1, array.getDouble(2));
   }

   boolean first = true;
   if (array != null){
     for(int k = 0; k < arrayDouble.size(); k++){
      if(first){
       first = false;
      }else{
       item.append(",");
      }
      if (arrayDouble.get(k) != null && !Double.isNaN(arrayDouble.get(k).doubleValue())){
       item.append(arrayDouble.get(k).floatValue());
      }else{
       item.append(0);
      }
     }
   }
   item.append("}");
  }else if(columnTypeName.endsWith(OraSqlType.array_Types[1])){//int
   BigDecimal[] array = (BigDecimal[])rs.getArray(i).getArray();
   boolean first = true;
   if (array != null){
     for(int k = 0; k < array.length; k++){
      if(first){
       first = false;
      }else{
       item.append(",");
      }
      if (array[k] != null){
       item.append(array[k].intValue());
      }else{
       item.append(0);
      }
     }
   }
   item.append("}");
  }else if(columnTypeName.endsWith(OraSqlType.array_Types[2])){//varchar
   String[] array = (String[])rs.getArray(i).getArray();
   boolean first = true;
   if (array != null){
     for(int k = 0; k < array.length; k++){
      if(first){
       first = false;
      }else{
       item.append(",");
      }
      if (array[k] != null){
       item.append(array[k]);
      }else{
       item.append("");
      }
     }
   }
   item.append("}");
  }
  return item.toString();
 }
 public static boolean isGreenplum(Connection conn){
  String sql = "select alpine_miner_get_dbtype()";
  itsLogger.debug("AlpineUtil.isGreenplum():sql="+sql);
  boolean ret = false;
  try {
   Statement st = conn.createStatement();
   ResultSet rs = st.executeQuery(sql);
   if (rs.next()){
    if (rs.getString(1).equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
     ret =  true;
    }
   }
  } catch (SQLException e) {
  }
  return ret;
 }
 
 public static void dropTable(Connection conn,String schemaName,String tableName,String outputType,String dbType) throws SQLException{
  ISqlGeneratorMultiDB sourceInfo = SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
  String execSql=null;
  if(outputType.equals(com.alpine.utility.db.Resources.OutputTypes[0])){//output:Table
   execSql=sourceInfo.dropTableIfExists(StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName));
  }else if(outputType.equals(com.alpine.utility.db.Resources.OutputTypes[1])){//View
   execSql=sourceInfo.dropViewIfExists(StringHandler.doubleQ(schemaName)+"."+StringHandler.doubleQ(tableName));
  } 

  Statement st=conn.createStatement();
  
  itsLogger.debug(execSql);
  
  st.execute(execSql);

 }
 

    public static List<String> initValue(String delimiter,String quoteChar,String escapChar,String str) throws Exception{
        List<String> list = new ArrayList<String>();
        boolean isquote = false;
        if(quoteChar != null && quoteChar.length() > 0)
            isquote = true;

        int index = str.indexOf(delimiter);
        while (index >= 0) {
            String value = str.substring(0, index);
            int subIndex = str.indexOf(delimiter) + delimiter.length();
            if(value == null || value.length() == 0)
                value = null;
            else{
                if (isquote) {
                    if (value.startsWith(quoteChar)) {
                        value = findSecendQuote(str.substring(quoteChar.length()),quoteChar,escapChar,delimiter);
                        subIndex = quoteChar.length() + value.length()+ quoteChar.length() + delimiter.length();
                    }

                    String s = value;
                    while(s.indexOf(quoteChar) >= 0){
                        if(!s.substring(0, s.indexOf(quoteChar)).endsWith(escapChar)){
                            throw new Exception("Quote char lack of data:"+value);
                        }
                        s = s.substring(s.indexOf(quoteChar)+quoteChar.length());
                    }
                }
            }
            if(value!=null&&value.length() == 0){
                value=null;
            }
            list.add(value);
            if(subIndex > str.length())
                subIndex --;
            str = str.substring(subIndex);
            index = str.indexOf(delimiter);
        }
        if(str == null || str.length() == 0)
            str = null;
        else
        {
            if (isquote) {
                if (str.startsWith(quoteChar)) {
                    str = findSecendQuote(str.substring(quoteChar.length()),quoteChar,escapChar,delimiter);
                }

                String s = str;
                if(s != null){
                    while(s.indexOf(quoteChar) >= 0)
                    {
                        if(!s.substring(0, s.indexOf(quoteChar)).endsWith(escapChar))
                        {
                            throw new Exception("Quote char lack of data:"+str);
                        }
                        s = s.substring(s.indexOf(quoteChar)+quoteChar.length());
                    }
                }
            }
        }

        list.add(str);
        return list;
    }

    private static String findSecendQuote(String s, String quoteChar, String escapChar, String delimiter) throws Exception {
        String value = "";
        if(s.indexOf(quoteChar) < 0 )
        {
            throw new Exception("Quote char lack of data:"+s);
        }
        value = s.substring(0 , s.indexOf(quoteChar));
        String ss = value;
        int count = 0;
        int lastIndex = -1;
        if (escapChar != null && escapChar.length() > 0)
            lastIndex = ss.lastIndexOf(escapChar);
        while( lastIndex >= 0 && lastIndex==(ss.length()-1)){
            count ++ ;
            ss = ss.substring(0, ss.length() - escapChar.length());
            lastIndex = ss.lastIndexOf(escapChar);
        }

        if(count != 0 && count % 2 != 0 )
        {
            value = value + quoteChar + findSecendQuote(s.substring(value.length()+quoteChar.length()), quoteChar, escapChar ,delimiter);
        }
        if(!(value.length()== s.length() || value.length()+quoteChar.length() == s.length()) && !s.substring(0, value.length() + quoteChar.length()+ delimiter.length()).endsWith(quoteChar+delimiter))
        {
            value = value + quoteChar + findSecendQuote(s.substring(value.length()+quoteChar.length()), quoteChar, escapChar ,delimiter);
        }
        return value;
    }
    
    /**
     * Returns customized pure file name that will replace the filenames
     * special characters with X
     *
     * @param  fileName HDFS file location
     * @return      PureFileName with special characters replaced X
     */
	public static  String getPureHadoopFileName(String fileName) {
		String pureFileName = getRealFileName(fileName);
		// please be careful this will generate the duplicated code, so use my way
		//pureFileName= pureFileName.replaceAll("[^a-zA-Z]+", "X");
		int hashCode = pureFileName.hashCode(); 
		if(hashCode<0){
			hashCode=Math.abs( hashCode);
		}
		  pureFileName= "file"+hashCode;
		return pureFileName;
	}
	public static String getRealFileName(String fileName) {
		int lastIndex = fileName.lastIndexOf(".");
		int lastSepIndex = fileName.lastIndexOf(HadoopFile.SEPARATOR);
		
		if(lastIndex <= lastSepIndex){
			lastIndex = fileName.length();
		}
		String pureFileName=fileName.substring(lastSepIndex+1,  lastIndex);
		return pureFileName;
	}
	
	public static String generateClusterName(List<String> list){
		String cluster = alpine_cluster;
		if(list == null){
			return cluster;
		}
		int id = 1;
		if(!list.contains(cluster)){
			return cluster;
		}
		while(true){
			if(list.contains(cluster+"_"+id)){
				id++;
			}else{
				break;
			}
		}
		return cluster+"_"+id;
	}
	
	public static String guessDBDataType(String dbSystem,String hadoopColumnType) {
		DataSourceType dataType = DataSourceType.getDataSourceType(dbSystem);
		if(dataType!=null){
			if(hadoopColumnType.equals(HadoopDataType.INT)){
				return dataType.getIntegerType();
            } else if (hadoopColumnType.equals(HadoopDataType.LONG)) {
                return dataType.getIdType();
            } else if (hadoopColumnType.equals(HadoopDataType.DOUBLE)
					||hadoopColumnType.equals(HadoopDataType.FLOAT)){
				return dataType.getDoubleType();
			}else{
				return dataType.getTextType();
			}
		}
		return null;
	}
	
	
}