/**
 * ClassName PigSeriverFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.pig;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.impl.util.PropertiesUtil;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.fs.HadoopRestCallerManager;

/**
 * @author Jeff Dong
 *
 */
public class PigServerFactory {
	private static final Logger itsLogger= Logger.getLogger(PigServerFactory.class);
	public static final PigServerFactory INSTANCE = new PigServerFactory();
	
	private static final String ALPINE_UTILITY_JAR = "AlpineUtility.jar";
	private static final String[] REQUIRED_JARS_FOR_PIG_OPERATORS={"AlpinePig.jar","log4j-1.2.17.jar","commons-httpclient-3.0.1.jar","AlpineHadoopAnalytics.jar"};
	private static final Map<String,String[]> VERSION_SPEC_JARS;
	static{
		VERSION_SPEC_JARS=new HashMap<String,String[]>();
		
		String[] cloudera34={"guava-r09-jarjar.jar"};
		VERSION_SPEC_JARS.put(HadoopConstants.VERSION_0_20_2_CDH3_U4,cloudera34);
	}
	static{
		
	}
	/**  We get this list by comparing the real pig job and our pig job*/
	static final String [] Duplicated_Property =new String[]{
		"java.home",
		"hadoop.tmp.dir",
		"java.endorsed.dirs",
		"java.vendor.url",
		"java.version",
		"package.definition",
		"java.vendor.url.bug",
		"user.timezone",
		"java.naming.factory.initial",
		"sun.java.command",
		"tomcat.util.buf.StringCache.byte.enabled",
		"package.access",
		"sun.desktop",
		"catalina.home",
		"java.specification.vendor",
		"java.library.path",
		"wtp.deploy",
		"java.class.version",
		"sun.boot.library.path",
		"dfs.data.dir",
		"dfs.support.append",
		"java.vm.specification.version",
		"dfs.name.dir",
		"java.ext.dirs",
		"os.version",
		"user.home",
		"java.vm.vendor",
		"user.dir",
		"common.loader",
		"dfs.replication",
		"catalina.base",
		"java.vm.version",
		"java.class.path",
		"java.vm.specification.vendor",
		"java.runtime.version",
		"sun.boot.class.path",
		"catalina.useNaming",
		"java.vendor Sun Microsystems",
		"java.naming.factory.url.pkgs",
		"java.specification.version"
	} ;
	
	private Properties nativeProperties;
	private PigServerFactory(){
		//save time
//		nativeProperties = new Properties();// PropertiesUtil.loadDefaultProperties();
		nativeProperties = PropertiesUtil.loadDefaultProperties();

	}

    public AlpinePigServer createPigServer(HadoopConnection hadoopConnection, boolean localmode,boolean isRest) throws  Exception{
    	if(isRest&&HadoopRestCallerManager.isRestEnabled()){
    		isRest=true;
    	}else{
    		isRest=false;
    	}
        Properties properties = generatePigProperties(hadoopConnection,localmode);
        if(isRest){
            return new AlpineRestPigServer(hadoopConnection,properties,localmode);
        }else{
            PigServer pigServer = createPigServerx(hadoopConnection,properties  );
            return new AlpineDirectPigServer(pigServer);
        }


    }

    public AlpinePigServer createPigServer(AlpineRestPigJsonObject wiredBack) throws Exception{
    	HadoopConnection connection = wiredBack.getHadoopConnection();
    	boolean isLocal=wiredBack.isLocal();
    	Properties localCreatedProps= generatePigProperties(wiredBack.getHadoopConnection(),wiredBack.isLocal());
    	Properties wiredProps=wiredBack.getProperties();
    	compareProps(localCreatedProps,wiredProps);
    	
    	
    	
        return new AlpineDirectPigServer(createPigServerx(connection,  generatePigProperties(connection,isLocal)));
    }

	private void compareProps(Properties lp,Properties wp) {
		Enumeration<Object> lpKeys = lp.keys();
		List<String> matched=new ArrayList<String>();
		List<String> notMatched=new ArrayList<String>();
		while(lpKeys.hasMoreElements()){
			Object next = lpKeys.nextElement();
			Object lpO = lp.get(next);
			
			Object wpO=wp.get(next);
			
			if(null==wpO){
				notMatched.add("Wired object doesnt have the property of-----["+wpO+"]-------");
				continue;
			}
			
			if(wpO.equals(lpO)){
				matched.add("Wired object property and locally created objecs are equal for the key ["+next+"]with the value of["+lpO+"]");
			}else{
				notMatched.add("Wired object property and locally created objecs are not equal for the key----- ["+next+"]-----with the value of-----["+lpO+"]-----on local and with the value of-----["+wpO+"]-----on wired");
			}
		}
		
		for(String msg:matched){
			itsLogger.info(msg);
			System.out.println(msg);
		}
		for(String msg:notMatched){
			itsLogger.error(msg);
			System.out.println(msg);
		}
		
		
	}

	public PigServer createPigServerx(HadoopConnection hadoopConnection, Properties properties   ) throws  Exception{


        PigServer pigServer = null;

		//this is very important for the kerberos ...
		Configuration conf = hadoopConnection.toHadoopConfiguration();
 
		HadoopConnection.SetConf4UserGroupInformation(conf);//UserGroupInformation.setConfiguration(conf);
 
		pigServer= new PigServer(ExecType.MAPREDUCE,properties);

 		String root=System.getProperty("ALPINE_PIG_JARS_ROOT");
		if(null!=root){
			registerJars(pigServer,REQUIRED_JARS_FOR_PIG_OPERATORS,root,hadoopConnection);
		}else{
			registerJars(pigServer,REQUIRED_JARS_FOR_PIG_OPERATORS,hadoopConnection);
		}
	    if(itsLogger.isDebugEnabled()){
	    	itsLogger.debug("Created a new pig server instance for the hadoop connection of["+hadoopConnection+"]with pig context of["+pigServer.getPigContext()+"]");
	    }
		return pigServer;
	}
	
	private void registerJars(PigServer pigServer,String[] jarNames,String rootDir, HadoopConnection hadoopConnection) throws URISyntaxException, IOException {
		for (String jn : jarNames) {	
			makeSureJarExist(rootDir+jn);
			pigServer.registerJar(rootDir+jn);
			System.err.println("Register the jar["+rootDir+jn+"]");
		}
		
		String[] customrJars = VERSION_SPEC_JARS.get(hadoopConnection.getVersion());
		if (null != customrJars && 0 != customrJars.length) {
			for (String jn : customrJars) {
				makeSureJarExist(rootDir+jn);
				pigServer.registerJar(rootDir+jn);
				System.err.println("Register the jar["+rootDir+jn+"]");
			}
		}
	}
    private void registerJars(PigServer pigServer,String[] jarNames,HadoopConnection hadoopConnection) {
        try {
            File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            String path = f.getPath();

            String systemName = System.getProperty("os.name");

            // [Pivotal 42187699]special prefix for windows ...
            if (systemName.toLowerCase().startsWith("window")&&HadoopConnection.CURRENT_HADOOP_VERSION.equals(HadoopConstants.VERSION_0_20_2_CDH3_U4)) {
                path = "file:///" + path;
            }
            
			for (String jn : jarNames) {	
				acquirePathOfTheJarAndRegisterIt(pigServer, path, jn);
			}
			
			String[] customrJars = VERSION_SPEC_JARS.get(hadoopConnection.getVersion());
			if (null != customrJars && 0 != customrJars.length) {
				for (String cj : customrJars) {
					acquirePathOfTheJarAndRegisterIt(pigServer, path, cj);
				}
			}
			
			


        } catch (Exception e) {
            itsLogger.error("Could not register the jar file will try to register it with default location",e);
            try{
                pigServer.registerJar("AlpinePig.jar");
            }catch(Exception e2){
                itsLogger.error("Could not register ti with default location either",e2);
            }
        }
    }

	private void acquirePathOfTheJarAndRegisterIt(PigServer pigServer, String path, String cj)
			throws URISyntaxException, IOException {
		String alpineHadoop = path.replace(ALPINE_UTILITY_JAR,cj);
		alpineHadoop = alpineHadoop.replace('\\', '/');
		makeSureJarExist(alpineHadoop);
		pigServer.registerJar(alpineHadoop);
		itsLogger.error("Register the jar["+alpineHadoop+"]");
	}
    
    
    private void makeSureJarExist(String alpineHadoop) throws URISyntaxException{
   	    File fAlpinePig = new File(alpineHadoop);
   	    boolean isFile = fAlpinePig.isFile();
   	    if(!isFile){
   	        String err=alpineHadoop+" is not found..";
   	        throw new IllegalStateException(err);
   	    }
   	
   }
   

    private Properties generatePigProperties(HadoopConnection hadoopConnection,boolean localmode) {
    	
        Properties properties =(Properties) nativeProperties.clone();
        properties.put("fs.default.name","hdfs://"+hadoopConnection.getHdfsHostName()+":"+hadoopConnection.getHdfsPort());
        properties.put("hadoop.job.ugi", hadoopConnection.getUserName()+","+hadoopConnection.getGroupName() );
        if(localmode==false){
            for (int i = 0; i < Duplicated_Property.length; i++) {
                properties.remove(Duplicated_Property[i]) ;
            }
            properties.put("mapred.job.tracker",hadoopConnection.getJobHostName()+":"+hadoopConnection.getJobPort());
        }


        properties.put("ipc.client.connect.max.retries.on.timeouts", "1"); //retry 1 times ()
        properties.put("ipc.client.connection.maxidletime", "5000"); //5s
        properties.put("ipc.client.connect.max.retries", "1");//retry 1 times ()

        hadoopConnection.fillSecurityInfo2Props(properties);
        return properties;
    }
	
}
