package com.alpine.datamining.api;

import java.util.*;

import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.hadoop.pig.PigServerFactory;

public class AnalyticContext {

    Logger logger = Logger.getLogger(AnalyticContext.class);
    private Properties props = new Properties();
    //This is used in case we generate more than
    private Map<HadoopConnection,AlpinePigServer> pigserverMap = new HashMap<HadoopConnection,AlpinePigServer>();
    private Map <String,Job> mapreduceJobMap = new HashMap <String,Job>();
	private List<String > emptyPigVariabelList = new ArrayList<String > ();
    private Map<String,String> pigVariables;//Data source name -->Pig Variable
    private Map<String,String> pigDesignTimeVariables;
    private boolean isLocalModelPig;
    private DataAnalyzer currenctAnalyzer;

    public DataAnalyzer getCurrenctAnalyzer() {
        return currenctAnalyzer;
    }


    public void setCurrenctAnalyzer(DataAnalyzer currenctAnalyzer) {
        this.currenctAnalyzer = currenctAnalyzer;
    }


    public boolean isLocalModelPig() {
        return isLocalModelPig;
    }


    public void setLocalModelPig(boolean isLocalModelPig) {
        this.isLocalModelPig = isLocalModelPig;
    }


    public AnalyticContext(){
        pigVariables = new HashMap<String,String>();
        pigDesignTimeVariables=new HashMap<String,String>();
    }


    public Map<String, String> getPigVariables() {
        return pigVariables;
    }

    public boolean addPigVariable(String fileName,String variableName){
        if(null==fileName||null==variableName||fileName.trim().equals("")||variableName.trim().equals("")){
            return false;
        }

        if(pigVariables.containsKey(fileName)){
            return false;
        }
        pigVariables.put(fileName,variableName);

        return true;

    }


    public String getPigVariableNameForTheFileOf(String fileName){
        if(null==fileName||fileName.trim().equals("")){
            return null;
        }
        return pigVariables.get(fileName);
    }

    public boolean addDesignTimePigVariable(String fileName,String variableName){
        if(null==fileName||null==variableName||fileName.trim().equals("")||variableName.trim().equals("")){
            return false;
        }

        if(pigDesignTimeVariables.containsKey(fileName)){
            return false;
        }
        pigDesignTimeVariables.put(fileName,variableName);

        return true;

    }


    public String getDesignTimePigVariableNameForTheFileOf(String fileName){
        if(null==fileName||fileName.trim().equals("")){
            return null;
        }
        return pigDesignTimeVariables.get(fileName);
    }

    public void setPigVariables(Map<String, String> pigVariables) {
        this.pigVariables = pigVariables;
    }


    public Object getProperty(Object key) {
        return props.get(key);
    }

    public void putProperty(Object key,Object value) {
        props.put(key, value);
    }

    public AlpinePigServer getPigServer(HadoopConnection hadoopConnection) throws Exception{
        if(pigserverMap.containsKey(hadoopConnection)==false){
            pigserverMap.put(hadoopConnection, PigServerFactory.INSTANCE.createPigServer(hadoopConnection,isLocalModelPig,true));
        }
        return pigserverMap.get(hadoopConnection) ;
    }

    public void registerMapReduceJob(Job job){
        mapreduceJobMap.put(job.getJobName(), job)  ;
    }

    //will call this while the flow is finished or stopped
    public void dispose(){
        if(currenctAnalyzer!=null){
            currenctAnalyzer.stop();
        }
        Set<HadoopConnection> keys = pigserverMap.keySet();
        for(HadoopConnection hadoopConnection:keys){
            AlpinePigServer pigServer = pigserverMap.get(hadoopConnection);
            if(pigServer!=null){
                pigServer.shutdown();
            }

        }
        pigserverMap.clear();
		emptyPigVariabelList.clear();
        cancleHadoopJob();
        currenctAnalyzer=null;
    }


    public void cancleHadoopJob() {
        Collection<Job> jobs = mapreduceJobMap.values();
        if(currenctAnalyzer!=null){
            currenctAnalyzer.stop();
        }
        for (Iterator<Job> iterator = jobs.iterator(); iterator.hasNext();) {
            Job job = iterator.next();
            try{
                if(job.isComplete()==false){
                    job.killJob();
                }
            }catch(Exception e){
                logger.error("unable to kill job :"+job.getJobName(), e);
            }

        }

        mapreduceJobMap.clear();


	}


	public void addEmptyPigVariabel(String outputTempFileName) {
		if(emptyPigVariabelList.contains(outputTempFileName)==false){
			emptyPigVariabelList.add(outputTempFileName) ;
		}

	}


	public void removeEmptyPigVariabel(String outputTempFileName) {
		if(emptyPigVariabelList.contains(outputTempFileName)==true){
			emptyPigVariabelList.remove(outputTempFileName) ;
		}

	}


	public boolean isEmptyPigVariable(String outputTempFileName) {
		return emptyPigVariabelList.contains(outputTempFileName);
    }



}
